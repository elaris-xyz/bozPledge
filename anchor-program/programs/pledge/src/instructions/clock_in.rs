use anchor_lang::prelude::*;
use crate::errors::PledgeError;
use crate::state::{Commitment, ClockInEvent};

#[derive(Accounts)]
pub struct ClockIn<'info> {
    /// Can be the main user wallet OR the local device session key.
    pub signer: Signer<'info>,

    #[account(
        mut,
        constraint = !commitment.settled @ PledgeError::AlreadySettled,
    )]
    pub commitment: Account<'info, Commitment>,
}

pub fn handle_clock_in(
    ctx: Context<ClockIn>,
    day_index: u8,
    steps_reported: u32,
) -> Result<()> {
    let commitment = &mut ctx.accounts.commitment;
    let signer_key = ctx.accounts.signer.key();

    // Verify authorization: must be either the user wallet or the delegated device key
    let is_owner = signer_key == commitment.authority;
    let is_session_key = commitment.clock_in_authority != Pubkey::default() && signer_key == commitment.clock_in_authority;

    require!(is_owner || is_session_key, PledgeError::UnauthorizedClockIn);

    // Verify step count requirement
    require!(steps_reported >= commitment.target_steps, PledgeError::TargetNotMet);

    // Verify day index validity
    require!(day_index < commitment.total_days, PledgeError::InvalidDayWindow);

    // Verify day has not been clocked in yet
    let day_mask = 1u64 << (day_index as u64);
    require!((commitment.clocked_in_bitmap & day_mask) == 0, PledgeError::DayAlreadyClockedIn);

    // Verify time window
    let now = Clock::get()?.unix_timestamp;
    let day_start = commitment
        .start_timestamp
        .checked_add((day_index as i64) * (commitment.day_duration_sec as i64))
        .ok_or(PledgeError::MathOverflow)?;
    let day_end = day_start
        .checked_add(commitment.day_duration_sec as i64)
        .ok_or(PledgeError::MathOverflow)?;

    require!(now >= day_start && now < day_end, PledgeError::InvalidDayWindow);

    // Mark day as clocked in
    commitment.clocked_in_bitmap |= day_mask;
    commitment.completed_days = commitment
        .completed_days
        .checked_add(1)
        .ok_or(PledgeError::MathOverflow)?;

    emit!(ClockInEvent {
        commitment: commitment.key(),
        day_index,
        steps_reported,
        completed_days_so_far: commitment.completed_days,
        timestamp: now,
    });

    Ok(())
}
