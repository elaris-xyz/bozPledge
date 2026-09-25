use anchor_lang::prelude::*;

#[account]
pub struct Commitment {
    /// The user wallet who created the commitment and staked tokens.
    pub authority: Pubkey,

    /// Optional local ephemeral session key generated on Android device.
    /// Can sign daily clock_in without prompting wallet UI.
    pub clock_in_authority: Pubkey,

    /// The SPL token mint (e.g. $SKR mint or mock SKR).
    pub token_mint: Pubkey,

    /// The PDA vault holding the staked tokens.
    pub vault: Pubkey,

    /// Daily target step count (e.g., 5000, 8000, 10000).
    pub target_steps: u32,

    /// Total number of days in the challenge (1 to 64).
    pub total_days: u8,

    /// Number of successfully clocked-in days.
    pub completed_days: u8,

    /// Duration of each "day" in seconds (86400 in production, 30s in demo mode).
    pub day_duration_sec: u64,

    /// Start timestamp (Unix epoch seconds).
    pub start_timestamp: i64,

    /// Total tokens staked for the entire challenge.
    pub total_amount: u64,

    /// Whether this commitment has been settled.
    pub settled: bool,

    /// Bitmask storing completion status for up to 64 days.
    /// Bit `i` is set to 1 if day `i` was successfully clocked in.
    pub clocked_in_bitmap: u64,

    /// PDA bump for the commitment account.
    pub bump: u8,

    /// PDA bump for the token vault account.
    pub vault_bump: u8,
}

impl Commitment {
    pub const LEN: usize = 8 + // discriminator
        32 + // authority
        32 + // clock_in_authority
        32 + // token_mint
        32 + // vault
        4  + // target_steps
        1  + // total_days
        1  + // completed_days
        8  + // day_duration_sec
        8  + // start_timestamp
        8  + // total_amount
        1  + // settled
        8  + // clocked_in_bitmap
        1  + // bump
        1  + // vault_bump
        32;  // reserved buffer for future extensions
}

#[event]
pub struct CommitmentCreatedEvent {
    pub commitment: Pubkey,
    pub authority: Pubkey,
    pub token_mint: Pubkey,
    pub total_amount: u64,
    pub target_steps: u32,
    pub total_days: u8,
    pub day_duration_sec: u64,
    pub start_timestamp: i64,
}

#[event]
pub struct ClockInEvent {
    pub commitment: Pubkey,
    pub day_index: u8,
    pub steps_reported: u32,
    pub completed_days_so_far: u8,
    pub timestamp: i64,
}

#[event]
pub struct SettledEvent {
    pub commitment: Pubkey,
    pub user: Pubkey,
    pub refunded_amount: u64,
    pub burned_amount: u64,
    pub completed_days: u8,
    pub total_days: u8,
}
