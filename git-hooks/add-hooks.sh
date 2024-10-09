#!/bin/sh

root="$(pwd)"
echo "Root: $root"


cp -v "$root/pre-commit" "../.git/hooks/pre-commit"
cp -v "$root/commit-msg" "../.git/hooks/commit-msg"

chmod ug+x "../.git/hooks/"

echo "Done"