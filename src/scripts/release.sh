#!/usr/bin/env bash
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Sets release version, creates tag, and pushes tag to 'origin'
# ----------------------------------------------------------------------------
script_name=$0
function usage {
    echo "usage: ${script_name} [release-version]"
    echo "  Sets release version, creates tag, and pushes tag to 'origin'"
    exit 1
}

if [ "$#" -ne 1 ]; then
  usage
fi

version="${1}"
tag="v${version}"
echo "Updating version to: ${version}"
./mvnw versions:set -DnewVersion=${version} -N

git commit -am "[release] Setting version to ${version}"

echo "Creating the tag: ${tag}"
git tag --sign ${tag} -m "[release] ${version}"
git push origin ${tag}
