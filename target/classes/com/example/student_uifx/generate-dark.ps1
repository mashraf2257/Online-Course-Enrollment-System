# Generate theme-dark.css from theme.css by replacing all colors
$css = Get-Content -Path "$PSScriptRoot\theme.css" -Raw

# Root variables
$css = $css -replace '-fx-background-color: #F3F6FB;', '-fx-background-color: #111827;'
$css = $css -replace '-app-bg: #F3F6FB;', '-app-bg: #111827;'
$css = $css -replace '-app-surface: #ffffff;', '-app-surface: #1F2937;'
$css = $css -replace '-app-surface-muted: #f3f5f7;', '-app-surface-muted: #1a2332;'
$css = $css -replace '-app-border: #c8d0d8;', '-app-border: #374151;'
$css = $css -replace '-app-border-soft: #e2e7ec;', '-app-border-soft: #374151;'
$css = $css -replace '-app-text: #111827;', '-app-text: #F9FAFB;'
$css = $css -replace '-app-text-muted: #4b5563;', '-app-text-muted: #D1D5DB;'
$css = $css -replace '-app-text-soft: #6b7280;', '-app-text-soft: #9CA3AF;'
$css = $css -replace '-app-primary: #2f8fd8;', '-app-primary: #3B82F6;'
$css = $css -replace '-app-primary-dark: #1f6fb2;', '-app-primary-dark: #2563EB;'
$css = $css -replace '-app-primary-soft: #e9f2fa;', '-app-primary-soft: #1E3A5F;'
$css = $css -replace '-app-sidebar-bg: #F3F6FB;', '-app-sidebar-bg: #0F172A;'
$css = $css -replace '-app-sidebar-line: #c8d0d8;', '-app-sidebar-line: #1E293B;'
$css = $css -replace '-app-sidebar-text: #26384f;', '-app-sidebar-text: #E2E8F0;'
$css = $css -replace '-app-sidebar-text-muted: #4b5563;', '-app-sidebar-text-muted: #94A3B8;'
$css = $css -replace '-app-sidebar-active: #2f8fd8;', '-app-sidebar-active: #60A5FA;'
$css = $css -replace '-app-sidebar-active-bg: #edf2f5;', '-app-sidebar-active-bg: #1E293B;'
$css = $css -replace '-app-success: #0f7a5a;', '-app-success: #34D399;'
$css = $css -replace '-app-success-bg: #dff4ee;', '-app-success-bg: #064E3B;'
$css = $css -replace '-app-danger: #b84a4a;', '-app-danger: #F87171;'
$css = $css -replace '-app-danger-bg: #f8e4e5;', '-app-danger-bg: #7F1D1D;'
$css = $css -replace '-app-draft: #4b5563;', '-app-draft: #9CA3AF;'
$css = $css -replace '-app-draft-bg: #e5e7eb;', '-app-draft-bg: #374151;'

# Backgrounds: white -> dark surface
$css = $css -replace '-fx-background-color: white;', '-fx-background-color: #1F2937;'
$css = $css -replace '-fx-background-color: #FFFFFF;', '-fx-background-color: #1F2937;'

# Light grays -> dark grays (backgrounds)
$css = $css -replace '-fx-background-color: #F3F6FB;', '-fx-background-color: #111827;'
$css = $css -replace '-fx-background-color: #F9FAFB;', '-fx-background-color: #1a2332;'
$css = $css -replace '-fx-background-color: #F3F4F6;', '-fx-background-color: #374151;'
$css = $css -replace '-fx-background-color: #F7F7F2;', '-fx-background-color: #1a2332;'
$css = $css -replace '-fx-background-color: #EBEBEB;', '-fx-background-color: #374151;'
$css = $css -replace '-fx-background-color: #FAFAFF;', '-fx-background-color: #1a2332;'
$css = $css -replace '-fx-background-color: #E8F3EE;', '-fx-background-color: #064E3B;'

# Status badge backgrounds (light -> dark muted)
$css = $css -replace '-fx-background-color: #D1FAE5;', '-fx-background-color: #064E3B;'
$css = $css -replace '-fx-background-color: #FEE2E2;', '-fx-background-color: #7F1D1D;'
$css = $css -replace '-fx-background-color: #FEF3C7;', '-fx-background-color: #78350F;'
$css = $css -replace '-fx-background-color: #DBEAFE;', '-fx-background-color: #1E3A5F;'
$css = $css -replace '-fx-background-color: #EEF2FF;', '-fx-background-color: #1E3A5F;'
$css = $css -replace '-fx-background-color: #ECFDF5;', '-fx-background-color: #064E3B;'
$css = $css -replace '-fx-background-color: #FEF2F2;', '-fx-background-color: #7F1D1D;'
$css = $css -replace '-fx-background-color: #EFF6FF;', '-fx-background-color: #1E3A5F;'
$css = $css -replace '-fx-background-color: #EDE9FE;', '-fx-background-color: #2E1065;'

# Border colors: light -> dark
$css = $css -replace '-fx-border-color: #E5E7EB;', '-fx-border-color: #374151;'
$css = $css -replace '-fx-border-color: #D1D5DB;', '-fx-border-color: #4B5563;'
$css = $css -replace '-fx-border-color: #DCE0E5;', '-fx-border-color: #374151;'
$css = $css -replace '-fx-border-color: #EEF0F3', '-fx-border-color: #374151'
$css = $css -replace '-fx-border-color: #B8D8C8;', '-fx-border-color: #065F46;'
$css = $css -replace '-fx-border-color: #C4CAD2;', '-fx-border-color: #4B5563;'
$css = $css -replace '-fx-border-color: #9CA3AF;', '-fx-border-color: #6B7280;'
$css = $css -replace '-fx-border-color: #B0B8C4;', '-fx-border-color: #4B5563;'
$css = $css -replace '-fx-border-color: #6EE7B7;', '-fx-border-color: #065F46;'
$css = $css -replace '-fx-border-color: #FCA5A5;', '-fx-border-color: #991B1B;'
$css = $css -replace '-fx-border-color: #1D4ED8;', '-fx-border-color: #2563EB;'

# Text fills: dark -> light
$css = $css -replace '-fx-text-fill: #111827;', '-fx-text-fill: #F9FAFB;'
$css = $css -replace '-fx-text-fill: #0F172A;', '-fx-text-fill: #F9FAFB;'
$css = $css -replace '-fx-text-fill: #1F2937;', '-fx-text-fill: #E5E7EB;'
$css = $css -replace '-fx-text-fill: #293241;', '-fx-text-fill: #D1D5DB;'
$css = $css -replace '-fx-text-fill: #374151;', '-fx-text-fill: #D1D5DB;'
$css = $css -replace '-fx-text-fill: #4B5563;', '-fx-text-fill: #9CA3AF;'
$css = $css -replace '-fx-text-fill: #6B7280;', '-fx-text-fill: #9CA3AF;'
$css = $css -replace '-fx-text-fill: #6b7280;', '-fx-text-fill: #9CA3AF;'
$css = $css -replace '-fx-text-fill: #9CA3AF;', '-fx-text-fill: #9CA3AF;'
$css = $css -replace '-fx-text-fill: #1E3A8A;', '-fx-text-fill: #93C5FD;'
$css = $css -replace '-fx-text-fill: #1267a9;', '-fx-text-fill: #60A5FA;'

# Status text fills
$css = $css -replace '-fx-text-fill: #065F46;', '-fx-text-fill: #6EE7B7;'
$css = $css -replace '-fx-text-fill: #047857;', '-fx-text-fill: #6EE7B7;'
$css = $css -replace '-fx-text-fill: #B91C1C;', '-fx-text-fill: #FCA5A5;'
$css = $css -replace '-fx-text-fill: #991B1B;', '-fx-text-fill: #FCA5A5;'
$css = $css -replace '-fx-text-fill: #DC2626;', '-fx-text-fill: #F87171;'
$css = $css -replace '-fx-text-fill: #92400E;', '-fx-text-fill: #FDE68A;'
$css = $css -replace '-fx-text-fill: #B45309;', '-fx-text-fill: #FDE68A;'
$css = $css -replace '-fx-text-fill: #1E40AF;', '-fx-text-fill: #93C5FD;'
$css = $css -replace '-fx-text-fill: #1D4ED8;', '-fx-text-fill: #93C5FD;'
$css = $css -replace '-fx-text-fill: #2563EB;', '-fx-text-fill: #60A5FA;'
$css = $css -replace '-fx-text-fill: #166534;', '-fx-text-fill: #6EE7B7;'
$css = $css -replace '-fx-text-fill: #365046;', '-fx-text-fill: #A7F3D0;'
$css = $css -replace '-fx-text-fill: #1F1F1F;', '-fx-text-fill: #F9FAFB;'

# MC purple accents
$css = $css -replace '-fx-text-fill: #7C3AED;', '-fx-text-fill: #A78BFA;'
$css = $css -replace '-fx-text-fill: #5B21B6;', '-fx-text-fill: #C4B5FD;'

# Misc backgrounds
$css = $css -replace '-fx-background-color: #E5E7EB;', '-fx-background-color: #374151;'
$css = $css -replace '-fx-background-color: #111827;', '-fx-background-color: #0F172A;'
$css = $css -replace '-fx-prompt-text-fill: #a8b1bd;', '-fx-prompt-text-fill: #6B7280;'
$css = $css -replace '-fx-prompt-text-fill: #9CA3AF;', '-fx-prompt-text-fill: #6B7280;'
$css = $css -replace '-fx-background-color: #6B7280;', '-fx-background-color: #9CA3AF;'

# Table/border line colors using F3F4F6
$css = $css -replace '-fx-border-color: #F3F4F6', '-fx-border-color: #374151'

# Scrollbar thumbs - make lighter for dark bg
$css = $css -replace 'rgba\(0, 0, 0, 0\.15\)', 'rgba(255, 255, 255, 0.20)'
$css = $css -replace 'rgba\(0, 0, 0, 0\.25\)', 'rgba(255, 255, 255, 0.35)'
$css = $css -replace 'rgba\(0, 0, 0, 0\.12\)', 'rgba(255, 255, 255, 0.15)'

# Action button danger
$css = $css -replace '-fx-background-color: #2563EB;', '-fx-background-color: #3B82F6;'

# mc-content background
$css = $css -replace '-fx-background-color: #F3F6FB;', '-fx-background-color: #111827;'

# Add header comment
$css = "/* DARK THEME - Auto-generated from theme.css */`n" + $css

Set-Content -Path "$PSScriptRoot\theme-dark.css" -Value $css -NoNewline
Write-Host "theme-dark.css generated successfully"
