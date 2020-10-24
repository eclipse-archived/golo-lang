#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

echo "🚀 Preparing to deploy..."

echo "🔑 Decrypting files..."

gpg --quiet --batch --yes --decrypt --passphrase="${GPG_SECRET}" \
    --output golo-dev-sign.asc .build/golo-dev-sign.asc.gpg

gpg --quiet --batch --yes --decrypt --passphrase="${GPG_SECRET}" \
    --output gradle.properties .build/gradle.properties.gpg

gpg --fast-import --no-tty --batch --yes golo-dev-sign.asc

echo "📦 Publishing..."

./gradlew publish

echo "🧹 Cleanup..."

rm gradle.properties golo-dev-sign.asc

echo "✅ Done!"
