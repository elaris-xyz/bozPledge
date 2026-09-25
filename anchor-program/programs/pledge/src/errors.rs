use anchor_lang::prelude::*;

#[error_code]
pub enum PledgeError {
    #[msg("Reported step count is below the daily target.")]
    TargetNotMet,

    #[msg("You have already clocked in for this day.")]
    DayAlreadyClockedIn,

    #[msg("The current time does not match this day's clock-in window.")]
    InvalidDayWindow,

    #[msg("Commitment duration has not ended yet. Cannot settle early.")]
    CommitmentNotEnded,

    #[msg("This commitment has already been settled.")]
    AlreadySettled,

    #[msg("Signer is not authorized to clock in (must be user or local session key).")]
    UnauthorizedClockIn,

    #[msg("Total days must be between 1 and 64.")]
    InvalidTotalDays,

    #[msg("Day duration must be greater than zero seconds.")]
    InvalidDuration,

    #[msg("Arithmetic overflow occurred.")]
    MathOverflow,
}
