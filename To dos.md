# To dos

## Refactoring (next)
- Remove remaining direct WG entity field access (`wg.mitbewohner`, `wg.rooms`) in services/mappers; rely on repositories/DTOs.
- Add WG membership validation in finance create/update flows; stop direct `wg.mitbewohner` access.
- Introduce domain facades for finance/cleaning/shopping to move workflow logic out of controllers.
- Split `CleaningScheduleService` into smaller services; inject a clock/time provider.
- Encapsulate `WG` fields and add `equals/hashCode` where needed.
- Centralize UI utilities (dialogs, currency formatting, navigation) and fix encoding settings.

## Features
- Leaving WG: block leaving with negative balance; allow leaving with positive balance but warn.
- Transactions: improve wording for single-debtor case (e.g., "Daniel paid for [User]").
- Transaction history: include transactions of former members.
- Notifications for cleaning schedule and transactions.

## UI/Style
- Fix duplicate Transaction History header.
- Improve icons and dialog window styling.

## Data/Security
- Password hashing/encryption.
- Access handling and validation.

## Testing
- Add tests for balances, settlements, and cleaning schedule generation/rotation.

## Extras
- Settings: currency selection.
- Shopping list: payment link (optional).
