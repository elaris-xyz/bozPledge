// bozPledge Web App Logic

document.addEventListener('DOMContentLoaded', () => {
  // Screen Presentation Data
  const screenData = {
    onboarding: {
      title: "Dual Judge & MWA 2.0 Onboarding",
      badge: "Solana Mobile Stack",
      desc: "Designed specifically for hackathon evaluation and production Seeker devices. Judges can launch an instant pre-funded Devnet Ed25519 keypair in 1 tap, or connect any MWA 2.0 wallet (Seed Vault, Phantom, Solflare).",
      specs: [
        { label: "Protocol", val: "Mobile Wallet Adapter 2.0" },
        { label: "Network", val: "Solana Devnet" },
        { label: "Judge Bypass", val: "Instant Devnet Keypair" },
        { label: "Security", val: "Hardware Protected Enclave" }
      ],
      src: "assets/screen_onboarding.webp"
    },
    modal: {
      title: "1-Click Judge Devnet Keypair Initializer",
      badge: "Zero-Friction Sandbox",
      desc: "Hackathon judges don't need to manually configure wallets or search for faucets. The app instantly derives an on-device Ed25519 keypair and pre-funds it with 2,500 $SKR on Devnet with explicit confirmation.",
      specs: [
        { label: "Pre-funded Stake", val: "2,500 $SKR" },
        { label: "Devnet RPC", val: "api.devnet.solana.com" },
        { label: "Key Generation", val: "Native Ed25519 Keystore" },
        { label: "Confirmation", val: "Explicit Judge Dialog" }
      ],
      src: "assets/screen_judge_modal.webp"
    },
    dashboard: {
      title: "Hardware Telemetry & Sovereign Habit Card",
      badge: "Real-Time Oracle",
      desc: "Real-time step counter streaming from Samsung Galaxy Tab S7+ hardware pedometer. Shows active commitment progress, daily countdown deadline, and dynamic top Judge Sandbox shortcuts.",
      specs: [
        { label: "Hardware Sensor", val: "Android SensorManager / Health Connect" },
        { label: "Active Escrow", val: "1,000 $SKR Locked" },
        { label: "Daily Target", val: "10,000 Steps" },
        { label: "Hardware Proof", val: "Tamper-Resistant Telemetry" }
      ],
      src: "assets/screen_dashboard.webp"
    },
    clockin: {
      title: "1-Tap Daily Clock-In & Seed Vault Certification",
      badge: "Sub-Second UX",
      desc: "Daily clock-in signs sub-second transactions via an ephemeral session key stored in Android Keystore with zero annoying wallet popups. Day 1 is verified on-chain with green checkmark.",
      specs: [
        { label: "Execution Time", val: "< 400ms Local Sign" },
        { label: "Escrow Milestone", val: "Day 1 Verified & Intact" },
        { label: "Shareable", val: "Solana Blink on X ready" },
        { label: "Signature", val: "Seed Vault Certified #5Kz8..." }
      ],
      src: "assets/screen_clockedin.webp"
    },
    judgelab: {
      title: "Judge Evaluation Lab (Time Machine)",
      badge: "60-Second Hackathon Sandbox",
      desc: "Comprehensive evaluation bottom-sheet allowing judges to compress 7 days into 60 seconds. Test Day 1 pass, trigger a slashing breach, advance the clock, or reset state anytime.",
      specs: [
        { label: "Scenario 1", val: "Clock In Today (Goal Met / Pass)" },
        { label: "Scenario 2", val: "Trigger Slashing Breach (Missed Day)" },
        { label: "Scenario 3", val: "Advance Time Machine (+1 Day)" },
        { label: "Control", val: "Instant State Injection" }
      ],
      src: "assets/screen_judge_lab.webp"
    },
    catalog: {
      title: "Habit Commitment Catalog & Boz Philosophy",
      badge: "Mountain Goat Endurance",
      desc: "Explore verified hardware blueprints: 10,000 Steps Daily, 6:00 AM Club (NTP Clock), Odd-Days Internet Detox (NetworkStats socket monitor), or compose custom sensor rules.",
      specs: [
        { label: "Detox Oracle", val: "Android NetworkStatsManager" },
        { label: "Wake-Up Oracle", val: "Hardware NTP Clock" },
        { label: "Fitness Oracle", val: "Health Connect Step Cadence" },
        { label: "Philosophy", val: "Uncompromising Mountain Grit" }
      ],
      src: "assets/screen_catalog.webp"
    },
    ranks: {
      title: "Sovereign Community Ranks & Live Burn Feed",
      badge: "Deflationary Proof",
      desc: "Transparent leaderboard showing total active staked tokens, cumulative supply burned, success rates, and a live verifiable stream of on-chain attestations and slashing burns.",
      specs: [
        { label: "Active Staked", val: "284.5K $SKR" },
        { label: "Deflation Burned", val: "48,250 $SKR Permanently" },
        { label: "Protocol Success", val: "94.2% Attested" },
        { label: "Audit Stream", val: "Slot-by-Slot Block Verification" }
      ],
      src: "assets/screen_ranks.webp"
    },
    vault: {
      title: "Non-Custodial Vault & Soulbound Proof-of-Action cNFTs",
      badge: "Smart Contract Escrow",
      desc: "Inspect locked escrow balances, claim refunds upon cycle maturity, view earned Soulbound cNFT achievement badges, and audit every immutable transaction on Solana Devnet.",
      specs: [
        { label: "Smart Escrow", val: "PDA Vault [commitment.key()]" },
        { label: "Badges", val: "Soulbound Proof-of-Action cNFTs" },
        { label: "Transparency", val: "100% Non-Custodial Anchor Program" },
        { label: "Audit Trail", val: "Live Solana Explorer / Solscan Links" }
      ],
      src: "assets/screen_vault.webp"
    }
  };

  // Instant Asset Preloading for Zero Latency
  const preloadedImages = [];
  Object.values(screenData).forEach(item => {
    const img = new Image();
    img.src = item.src;
    preloadedImages.push(img);
  });

  // Screen Tab Interactivity
  const tabs = document.querySelectorAll('.screen-tab');
  const screenImg = document.getElementById('deviceScreenImg');
  const metaBadge = document.getElementById('metaBadge');
  const metaTitle = document.getElementById('metaTitle');
  const metaDesc = document.getElementById('metaDesc');
  const metaSpecs = document.getElementById('metaSpecs');

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      tabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');

      const screenKey = tab.getAttribute('data-screen');
      const data = screenData[screenKey];
      if (!data) return;

      // Smooth fade transition
      screenImg.style.opacity = '0';
      screenImg.style.transform = 'scale(0.98)';

      setTimeout(() => {
        screenImg.src = data.src;
        screenImg.alt = data.title;
        metaBadge.textContent = data.badge;
        metaTitle.textContent = data.title;
        metaDesc.textContent = data.desc;

        metaSpecs.innerHTML = data.specs.map(s => 
          `<li><span>${s.label}:</span> <strong>${s.val}</strong></li>`
        ).join('');

        screenImg.style.opacity = '1';
        screenImg.style.transform = 'scale(1)';
      }, 150);
    });
  });

  // Interactive Slashing Calculator
  const stakeInput = document.getElementById('stakeRange');
  const stakeDisplay = document.getElementById('stakeDisplay');
  const daysInput = document.getElementById('daysRange');
  const daysDisplay = document.getElementById('daysDisplay');

  const resRefund = document.getElementById('resRefund');
  const resRefundPct = document.getElementById('resRefundPct');
  const resBurn = document.getElementById('resBurn');
  const resBurnPct = document.getElementById('resBurnPct');

  function updateCalculator() {
    const stake = parseFloat(stakeInput.value) || 1000;
    const completedDays = parseInt(daysInput.value, 10) || 0;
    const totalDays = 7;

    stakeDisplay.textContent = `${stake.toLocaleString()} $SKR`;
    daysDisplay.textContent = `${completedDays} of ${totalDays} Days`;

    const refund = (completedDays / totalDays) * stake;
    const burn = stake - refund;
    const refundPct = Math.round((completedDays / totalDays) * 100);
    const burnPct = 100 - refundPct;

    resRefund.textContent = `${refund.toFixed(2)} $SKR`;
    resRefundPct.textContent = `(${refundPct}% refunded to wallet)`;

    resBurn.textContent = `${burn.toFixed(2)} $SKR`;
    resBurnPct.textContent = `(${burnPct}% burned via SPL token::burn)`;
  }

  if (stakeInput && daysInput) {
    stakeInput.addEventListener('input', updateCalculator);
    daysInput.addEventListener('input', updateCalculator);
    updateCalculator();
  }

  // 1-Click Copy to Clipboard
  window.copyToClipboard = function(text, btnElement) {
    navigator.clipboard.writeText(text).then(() => {
      const originalText = btnElement.innerHTML;
      btnElement.innerHTML = `✓ Copied!`;
      btnElement.style.background = '#00FFA3';
      btnElement.style.color = '#000';
      setTimeout(() => {
        btnElement.innerHTML = originalText;
        btnElement.style.background = '';
        btnElement.style.color = '';
      }, 2000);
    }).catch(err => {
      console.error('Failed to copy: ', err);
    });
  };
});
