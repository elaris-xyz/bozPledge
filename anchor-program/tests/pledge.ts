import * as anchor from "@coral-xyz/anchor";
import { Program } from "@coral-xyz/anchor";
import { Keypair, PublicKey, SystemProgram } from "@solana/web3.js";
import {
  TOKEN_PROGRAM_ID,
  createMint,
  createAccount,
  mintTo,
  getAccount,
} from "@solana/spl-token";
import { assert } from "chai";

describe("pledge", () => {
  const provider = anchor.AnchorProvider.env();
  anchor.setProvider(provider);

  // We can load the program from workspace
  const program = anchor.workspace.Pledge as Program<any>;

  let mint: PublicKey;
  let userTokenAccount: PublicKey;
  let user = provider.wallet;
  let sessionKeypair = Keypair.generate();

  const commitmentId = new anchor.BN(Date.now());
  const targetSteps = 5000;
  const totalDays = 3;
  const dayDurationSec = new anchor.BN(2); // 2-second days for fast automated test!
  const stakeAmount = new anchor.BN(300_000_000); // 300 SKR (with 6 decimals)

  let commitmentPda: PublicKey;
  let vaultPda: PublicKey;

  before(async () => {
    // 1. Create Mock SKR mint
    mint = await createMint(
      provider.connection,
      (user as any).payer,
      user.publicKey,
      null,
      6
    );

    // 2. Create User Token Account & Mint 1000 Mock SKR
    userTokenAccount = await createAccount(
      provider.connection,
      (user as any).payer,
      mint,
      user.publicKey
    );

    await mintTo(
      provider.connection,
      (user as any).payer,
      mint,
      userTokenAccount,
      user.publicKey,
      1_000_000_000
    );

    // 3. Derive PDAs
    [commitmentPda] = PublicKey.findProgramAddressSync(
      [
        Buffer.from("commitment"),
        user.publicKey.toBuffer(),
        commitmentId.toArrayLike(Buffer, "le", 8),
      ],
      program.programId
    );

    [vaultPda] = PublicKey.findProgramAddressSync(
      [Buffer.from("vault"), commitmentPda.toBuffer()],
      program.programId
    );
  });

  it("Creates a commitment with SKR staked into vault PDA", async () => {
    await program.methods
      .createCommitment(
        commitmentId,
        targetSteps,
        totalDays,
        dayDurationSec,
        stakeAmount
      )
      .accounts({
        user: user.publicKey,
        clockInAuthority: sessionKeypair.publicKey,
        commitment: commitmentPda,
        vault: vaultPda,
        userTokenAccount: userTokenAccount,
        tokenMint: mint,
        tokenProgram: TOKEN_PROGRAM_ID,
        systemProgram: SystemProgram.programId,
        rent: anchor.web3.SYSVAR_RENT_PUBKEY,
      })
      .rpc();

    const commitmentAccount = await program.account.commitment.fetch(commitmentPda);
    assert.equal(commitmentAccount.totalDays, 3);
    assert.equal(commitmentAccount.completedDays, 0);
    assert.equal(commitmentAccount.targetSteps, 5000);
    assert.isTrue(commitmentAccount.clockInAuthority.equals(sessionKeypair.publicKey));

    const vaultAccount = await getAccount(provider.connection, vaultPda);
    assert.equal(vaultAccount.amount.toString(), stakeAmount.toString());
  });

  it("Clocks in day 0 using local session key without wallet prompt", async () => {
    // Day 0 window is active right now
    await program.methods
      .clockIn(0, 5200)
      .accounts({
        signer: sessionKeypair.publicKey,
        commitment: commitmentPda,
      })
      .signers([sessionKeypair])
      .rpc();

    const commitmentAccount = await program.account.commitment.fetch(commitmentPda);
    assert.equal(commitmentAccount.completedDays, 1);
  });

  it("Rejects clock-in if steps target not met", async () => {
    try {
      await program.methods
        .clockIn(0, 3000)
        .accounts({
          signer: sessionKeypair.publicKey,
          commitment: commitmentPda,
        })
        .signers([sessionKeypair])
        .rpc();
      assert.fail("Should have thrown TargetNotMet error");
    } catch (err: any) {
      assert.include(err.toString(), "TargetNotMet");
    }
  });

  it("Settles after duration: refunds completed days and burns forfeited tokens", async () => {
    // Wait for commitment duration to pass (3 days * 2s = 6 seconds)
    await new Promise((resolve) => setTimeout(resolve, 6500));

    // Settle commitment
    await program.methods
      .settle()
      .accounts({
        caller: user.publicKey,
        user: user.publicKey,
        commitment: commitmentPda,
        vault: vaultPda,
        userTokenAccount: userTokenAccount,
        tokenMint: mint,
        tokenProgram: TOKEN_PROGRAM_ID,
      })
      .rpc();

    const commitmentAccount = await program.account.commitment.fetch(commitmentPda);
    assert.isTrue(commitmentAccount.settled);

    // Completed 1 out of 3 days -> 100 SKR refunded, 200 SKR burned!
    const userAccount = await getAccount(provider.connection, userTokenAccount);
    // Initial 1000 - 300 stake + 100 refund = 800
    assert.equal(userAccount.amount.toString(), "800000000");
  });
});
