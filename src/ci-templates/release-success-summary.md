The release of tag `${gitRef}` with POM version `${project.version}` has been staged ✅

The next step is to check if the release is reproducible by running a Maven build locally
using against the source dist zip and the tag.

For the source bundle you can run a command similar to:

```bash
# create a tmp dir
cd $(mktemp -d)

# Download the source bundle:
curl -s "${nexusStagingUrl}/org/apache/directory/scimple/scimple/${project.version}/scimple-${project.version}-source-release.zip" -O

# extract the source zip
unzip scimple-${project.version}-source-release.zip
cd scimple-${project.version}

# rebuild the source bundle and verify
./mvnw clean verify artifact:compare \
  -Papache-release \
  --threads=1 \
  -Dgpg.skip \
  -Dreference.repo=${nexusStagingUrl}
```

From your SCIMple git repository build the `${gitRef}` tag run:
```bash
# If you haven't cloned the repo, run:
# git clone ${project.scm.url}.git 
# cd directory-scimple

# checkout the tag
git checkout ${gitRef}

# rebuild the source bundle and verify
./mvnw clean verify artifact:compare \
  -Papache-release \
  --threads=1 \
  -Dgpg.skip \
  -Dreference.repo=${nexusStagingUrl}
```

If that was successful, start the vote email thread, suggested email template:

```txt
Subject: [VOTE] Release Apache Directory ${project.name} ${project.version}
  
This is a call to vote in favor of releasing Apache Directory ${project.name} version ${project.version}.

We solved # issues:
  <insert release notes or link to release notes> (see below for suggestion)

Maven Staging Repository: 
  ${nexusStagingUrl}

Dist Staging Url:
  ${svnDistUrl}

Guide to testing staged releases:
  https://maven.apache.org/guides/development/guide-testing-releases.html
  
Vote open for 72 hours.

[ ] +1
[ ] +0
[ ] -1 (please include reasoning)
```
