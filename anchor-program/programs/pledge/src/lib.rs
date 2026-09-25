use anchor_lang::prelude::*;

pub mod errors;
pub mod instructions;
pub mod state;

use instructions::*;

declare_id!("PLEDGE1111111111111111111111111111111111111");

#[program]
pub mod pledge {
    use super::*;

    /// Create a new pledge commitment, staking SKR tokens into an escrow PDA.
    pub fn create_commitment(
        ctx: Context<CreateCommitment>,
        commitment_id: u64,
        target_steps: u32,
        total_days: u8,
        day_duration_sec: u64,
        amount: u64,
    ) -> Result<()> {
        instructions::create_commitment::handle_create_commitment(
            ctx,
            commitment_id,
            target_steps,
            total_days,
            day_duration_sec,
            amount,
        )
    }

    /// Clock in daily. Can be signed by the user wallet or by the local device session key.
    pub fn clock_in(
        ctx: Context<ClockIn>,
        day_index: u8,
        steps_reported: u32,
    ) -> Result<()> {
        instructions::clock_in::handle_clock_in(ctx, day_index, steps_reported)
    }

    /// Settle the commitment after its duration ends.
    /// Returns completed fraction to user, burns failed fraction, and closes the vault.
    pub fn settle(ctx: Context<Settle>) -> Result<()> {
        instructions::settle::handle_settle(ctx)
    }
}
