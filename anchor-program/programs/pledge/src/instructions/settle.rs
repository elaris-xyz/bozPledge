use anchor_lang::prelude::*;
use anchor_spl::token::{self, Burn, CloseAccount, Mint, Token, TokenAccount, Transfer};
use crate::errors::PledgeError;
use crate::state::{Commitment, SettledEvent};

#[derive(Accounts)]
pub struct Settle<'info> {
    /// Permissionless caller (can be user or any crank / automated bot)
    #[account(mut)]
    pub caller: Signer<'info>,

    /// CHECK: The user who created the commitment and receives the refunded funds & rent
    #[account(
        mut,
        address = commitment.authority
    )]
    pub user: SystemAccount<'info>,

    #[account(
        mut,
        constraint = !commitment.settled @ PledgeError::AlreadySettled,
        constraint = commitment.vault == vault.key(),
        constraint = commitment.token_mint == token_mint.key(),
    )]
    pub commitment: Account<'info, Commitment>,

    #[account(
        mut,
        seeds = [b"vault", commitment.key().as_ref()],
        bump = commitment.vault_bump,
    )]
    pub vault: Account<'info, TokenAccount>,

    #[account(
        mut,
        constraint = user_token_account.owner == commitment.authority,
        constraint = user_token_account.mint == commitment.token_mint,
    )]
    pub user_token_account: Account<'info, TokenAccount>,

    #[account(mut)]
    pub token_mint: Account<'info, Mint>,

    pub token_program: Program<'info, Token>,
}

pub fn handle_settle(ctx: Context<Settle>) -> Result<()> {
    let commitment = &mut ctx.accounts.commitment;
    let now = Clock::get()?.unix_timestamp;

    // Verify commitment duration has passed
    let total_duration = (commitment.total_days as i64)
        .checked_mul(commitment.day_duration_sec as i64)
        .ok_or(PledgeError::MathOverflow)?;
    let end_timestamp = commitment
        .start_timestamp
        .checked_add(total_duration)
        .ok_or(PledgeError::MathOverflow)?;

    require!(now >= end_timestamp, PledgeError::CommitmentNotEnded);

    let total_amount = commitment.total_amount;
    let completed_days = commitment.completed_days as u64;
    let total_days = commitment.total_days as u64;

    // Calculate refund vs burn amounts
    let refund_amount = total_amount
        .checked_mul(completed_days)
        .ok_or(PledgeError::MathOverflow)?
        .checked_div(total_days)
        .ok_or(PledgeError::MathOverflow)?;

    let burn_amount = total_amount
        .checked_sub(refund_amount)
        .ok_or(PledgeError::MathOverflow)?;

    let commitment_key = commitment.key();
    let vault_seeds = &[
        b"vault",
        commitment_key.as_ref(),
        &[commitment.vault_bump],
    ];
    let signer_seeds = &[&vault_seeds[..]];

    // 1. Refund the completed portion to the user's token account
    if refund_amount > 0 {
        let transfer_cpi = Transfer {
            from: ctx.accounts.vault.to_account_info(),
            to: ctx.accounts.user_token_account.to_account_info(),
            authority: ctx.accounts.vault.to_account_info(),
        };
        let cpi_ctx = CpiContext::new_with_signer(
            ctx.accounts.token_program.to_account_info(),
            transfer_cpi,
            signer_seeds,
        );
        token::transfer(cpi_ctx, refund_amount)?;
    }

    // 2. Burn the forfeited portion ("From your failure, NO ONE profits, not even us")
    if burn_amount > 0 {
        let burn_cpi = Burn {
            mint: ctx.accounts.token_mint.to_account_info(),
            from: ctx.accounts.vault.to_account_info(),
            authority: ctx.accounts.vault.to_account_info(),
        };
        let cpi_ctx = CpiContext::new_with_signer(
            ctx.accounts.token_program.to_account_info(),
            burn_cpi,
            signer_seeds,
        );
        token::burn(cpi_ctx, burn_amount)?;
    }

    // 3. Close the vault token account and return rent to user
    let close_cpi = CloseAccount {
        account: ctx.accounts.vault.to_account_info(),
        destination: ctx.accounts.user.to_account_info(),
        authority: ctx.accounts.vault.to_account_info(),
    };
    let cpi_ctx = CpiContext::new_with_signer(
        ctx.accounts.token_program.to_account_info(),
        close_cpi,
        signer_seeds,
    );
    token::close_account(cpi_ctx)?;

    commitment.settled = true;

    emit!(SettledEvent {
        commitment: commitment.key(),
        user: commitment.authority,
        refunded_amount: refund_amount,
        burned_amount: burn_amount,
        completed_days: commitment.completed_days,
        total_days: commitment.total_days,
    });

    Ok(())
}
