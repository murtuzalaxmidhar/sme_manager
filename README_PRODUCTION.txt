LAX YARD & SME MANAGER - PRODUCTION GUIDE
========================================

Congratulations on installing Lax Yard & SME Manager! This guide explains how your data is managed and how you can keep it safe.

1. WHERE IS MY DATA STORED?
---------------------------
All your vendor information, purchase history, and settings are stored in a single database file.
On Windows, you can find it here:
%LOCALAPPDATA%\LaxSMEManager\lax_data.db

To open this folder, press Win+R, type "%LOCALAPPDATA%\LaxSMEManager", and press Enter.

2. IS MY DATA SAFE DURING UPDATES?
----------------------------------
Yes! When you install a new version of the application (e.g., Version 1.1 or 2.0), the installer only updates the program files in "C:\Program Files\". Your database folder in AppData is NOT touched. The new version will automatically find and use your existing data.

3. HOW TO PERFORM A MANUAL BACKUP
---------------------------------
It is highly recommended to perform regular backups of your data.
1. Close the application.
2. Go to %LOCALAPPDATA%\LaxSMEManager\
3. Copy the file "lax_data.db" to a safe location (like a USB drive, External Hard Drive, or Cloud Storage).

In case of a computer failure, you can simply paste this file back into the same folder on a new machine after installing the app.

4. NEED HELP?
-------------
For any support or bug reports, please contact RASolutions.

========================================
Version 2.0 (Premium Upgrade)
Developed by RASolutions
