use anchor_lang::prelude::*;
use anchor_spl::token::{self, Mint, Token, TokenAccount, Transfer};
use crate::errors::PledgeError;
use crate::state::{Commitment, CommitmentCreatedEvent};

#[derive(Accounts)]
#[instruction(commitment_id: u64, target_steps: u32, total_days: u8, day_duration_sec: u64, amount: u64)]
pub struct CreateCommitment<'info> {
    #[account(mut)]
    pub user: Signer<'info>,

    /// CHECK: Optional session key authorized to call clock_in. Can be Pubkey::default() if unused.
    pub clock_in_authority: UncheckedAccount<'info>,

    #[account(
        init,
        payer = user,
        space = Commitment::LEN,
        seeds = [b"commitment", user.key().as_ref(), &commitment_id.to_le_bytes()],
        bump
    )]
    pub commitment: Account<'info, Commitment>,

    #[account(
        init,
        payer = user,
        seeds = [b"vault", commitment.key().as_ref()],
        bump,
        token::mint = token_mint,
        token::authority = vault
    )]
    pub vault: Account<'info, TokenAccount>,

    #[account(
        mut,
        constraint = user_token_account.owner == user.key(),
        constraint = user_token_account.mint == token_mint.key()
    )]
    pub user_token_account: Account<'info, TokenAccount>,

    pub token_mint: Account<'info, Mint>,

    pub token_program: Program<'info, Token>,
    pub system_program: Program<'info, System>,
    pub rent: Sysvar<'info, Rent>,
}

pub fn handle_create_commitment(
    ctx: Context<CreateCommitment>,
    _commitment_id: u64,
    target_steps: u32,
    total_days: u8,
    day_duration_sec: u64,
    amount: u64,
) -> Result<()> {
    require!(total_days >= 1 && total_days <= 64, PledgeError::InvalidTotalDays);
    require!(day_duration_sec > 0, PledgeError::InvalidDuration);
    require!(amount > 0, PledgeError::MathOverflow);
    require!(target_steps > 0, PledgeError::TargetNotMet);

    let now = Clock::get()?.unix_timestamp;
    let commitment = &mut ctx.accounts.commitment;

    commitment.authority = ctx.accounts.user.key();
    commitment.clock_in_authority = ctx.accounts.clock_in_authority.key();
    commitment.token_mint = ctx.accounts.token_mint.key();
    commitment.vault = ctx.accounts.vault.key();
    commitment.target_steps = target_steps;
    commitment.total_days = total_days;
    commitment.completed_days = 0;
    commitment.day_duration_sec = day_duration_sec;
    commitment.start_timestamp = now;
    commitment.total_amount = amount;
    commitment.settled = false;
    commitment.clocked_in_bitmap = 0;
    commitment.bump = ctx.bumps.commitment;
    commitment.vault_bump = ctx.bumps.vault;

    // Transfer staked tokens (SKR) into PDA vault
    let cpi_accounts = Transfer {
        from: ctx.accounts.user_token_account.to_account_info(),
        to: ctx.accounts.vault.to_account_info(),
        authority: ctx.accounts.user.to_account_info(),
    };
    let cpi_ctx = CpiContext::new(ctx.accounts.token_program.to_account_info(), cpi_accounts);
    token::transfer(cpi_ctx, amount)?;

    emit!(CommitmentCreatedEvent {
        commitment: commitment.key(),
        authority: commitment.authority,
        token_mint: commitment.token_mint,
        total_amount: amount,
        target_steps,
        total_days,
        day_duration_sec,
        start_timestamp: now,
    });

    Ok(())
}
