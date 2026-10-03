const { Connection, Keypair, PublicKey, SystemProgram, Transaction, sendAndConfirmTransaction } = require('@solana/web3.js');
const { 
  TOKEN_PROGRAM_ID, 
  getAssociatedTokenAddress, 
  createAssociatedTokenAccountInstruction, 
  createMintToInstruction 
} = require('@solana/spl-token');

const RPC_URL = process.env.SOLANA_RPC_URL || 'https://api.devnet.solana.com';
const MINT_STR = 'F4L7W4qgFAuU2iyg5ePBENXTHeZyTPor4tJMfhUHqfgQ';
const MINT_PUBKEY = new PublicKey(MINT_STR);

// Deployer keypair bytes (Solana Devnet Hackathon Faucet Authority)
const DEPLOYER_SECRET = Uint8Array.from([
  167, 2, 67, 185, 201, 189, 200, 175, 226, 46, 43, 119, 30, 69, 241, 144, 
  117, 172, 197, 247, 224, 130, 82, 106, 218, 240, 246, 188, 203, 94, 180, 95, 
  164, 41, 114, 143, 5, 86, 232, 130, 125, 127, 85, 50, 111, 73, 17, 223, 
  176, 11, 62, 17, 0, 30, 63, 116, 152, 37, 64, 253, 97, 81, 49, 227
]);

module.exports = async function handler(req, res) {
  // Enable CORS
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader(
    'Access-Control-Allow-Headers',
    'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version'
  );

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  if (req.method === 'GET') {
    return res.status(200).json({
      service: 'bozPledge Solana Devnet Faucet',
      token: 'bozPledge SKR (Hackathon)',
      symbol: 'SKR',
      mint: MINT_STR,
      faucetAmount: 10000,
      decimals: 9,
      cluster: 'devnet',
      explorerUrl: `https://explorer.solana.com/address/${MINT_STR}?cluster=devnet`
    });
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  try {
    const { address, recipient, amount = 10000 } = req.body || {};
    const targetAddress = (address || recipient || '').trim();

    if (!targetAddress || targetAddress.length < 32 || targetAddress.length > 44) {
      return res.status(400).json({ error: 'Valid Solana Devnet public key is required' });
    }

    let recipientPubkey;
    try {
      recipientPubkey = new PublicKey(targetAddress);
    } catch (e) {
      return res.status(400).json({ error: `Invalid Solana address: ${targetAddress}` });
    }

    const connection = new Connection(RPC_URL, 'confirmed');
    const deployer = Keypair.fromSecretKey(DEPLOYER_SECRET);

    // 1. Check recipient SOL balance; top up with 0.1 SOL if < 0.05 SOL
    const currentSolLamports = await connection.getBalance(recipientPubkey);
    const currentSol = currentSolLamports / 1e9;

    const tx = new Transaction();

    if (currentSol < 0.05) {
      tx.add(
        SystemProgram.transfer({
          fromPubkey: deployer.publicKey,
          toPubkey: recipientPubkey,
          lamports: 100_000_000 // 0.1 SOL
        })
      );
    }

    // 2. Check/Derive Associated Token Account (supports on-curve wallets and off-curve judge keys)
    const recipientAta = await getAssociatedTokenAddress(MINT_PUBKEY, recipientPubkey, true);
    const ataAccount = await connection.getAccountInfo(recipientAta);

    if (!ataAccount) {
      tx.add(
        createAssociatedTokenAccountInstruction(
          deployer.publicKey,
          recipientAta,
          recipientPubkey,
          MINT_PUBKEY
        )
      );
    }

    // 3. Mint requested $SKR (default 10,000 * 10^9)
    const amountLamports = BigInt(Math.floor(amount * 1e9));
    tx.add(
      createMintToInstruction(
        MINT_PUBKEY,
        recipientAta,
        deployer.publicKey,
        amountLamports
      )
    );

    // 4. Send and confirm
    const signature = await sendAndConfirmTransaction(connection, tx, [deployer], {
      commitment: 'confirmed'
    });

    // 5. Query updated balances
    let newSkr = amount;
    try {
      const balRes = await connection.getTokenAccountBalance(recipientAta);
      newSkr = balRes.value.uiAmount || amount;
    } catch (_) {}

    const newSolLamports = await connection.getBalance(recipientPubkey);
    const newSol = newSolLamports / 1e9;

    return res.status(200).json({
      success: true,
      signature,
      mint: MINT_STR,
      recipient: targetAddress,
      recipientAta: recipientAta.toBase58(),
      amountMinted: amount,
      newSkrBalance: newSkr,
      newSolBalance: newSol,
      explorerUrl: `https://explorer.solana.com/tx/${signature}?cluster=devnet`
    });

  } catch (err) {
    console.error('Faucet Error:', err);
    return res.status(500).json({
      success: false,
      error: err.message || 'Internal Faucet Error',
      details: String(err)
    });
  }
};
