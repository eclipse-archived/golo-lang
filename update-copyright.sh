#!/bin/bash
# Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

set -e

PROJECT_ROOT="$(dirname "$(realpath "$0")")"
COPYRIGHT_START='Copyright (c) 2012'

function change {
  local to="${1:?Give new year to use}"
  grep --null --recursive --fixed-regexp --files-with-match \
    --exclude-dir='\.git/' --exclude-dir='build' \
    "$COPYRIGHT_START" "$PROJECT_ROOT" \
    | xargs --null --max-lines=1 --verbose \
      sed -i "s/$COPYRIGHT_START-[0-9]\{4,4\}/$COPYRIGHT_START-$to/"
}

default_year=$(date +%Y)
change "${1:-$default_year}"
