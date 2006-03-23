rem generates the MD5 hash of a file using the openssl binary
rem argument - filename to be md5 calculated
openssl md5 < %1 > %1.md5
gpg --armor --output %1.asc --detach-sig %1
gpg --verify %1.asc %1