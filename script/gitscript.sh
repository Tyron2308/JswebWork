#!/bin/bash
##git add .

first="$1"
second="$2"
third="$3"

echo "${first}"
echo "${second}"

do_pull() {
    echo 'Pull from repository' {$second}
    git pull origin "${second}"
}

if [ $third == 1 ]
then
    do_pull
else
    echo 'we dont need to pull'
fi

#git credential-cache exit
#git config credential.helper store
git add .
git commit -m "${first}"
git push origin "${second}"
