<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

Release Process
===============

Release process overview
------------------------

### Update: Since the 1.8.x series we have released from git master without branches. Skip to Performing a Release. We may or may not use branches again in the future. 

### Cutting a branch

*   When a release is ready to go, release manager (RM) puts
    forward a release plan as per standard Apache process, including
    dates. This gets VOTEd on by the committers. During this period the
    trunk is still the only relevant source base.

*   As soon as a release is approved (or even before), RM should
    add the new version into JIRA as a target.

*   At the point where we would normally do the "code freeze" for a
    release, the RM cuts a branch named for the release. This branch is
    where the release candidates and releases will happen.

*   Ideally a release branch is only around for a week or maybe two
    before the release happens.

*   The only things that should EVER get checked into the release
    branch are - 1) bug fixes targeted at the release, 2)
    release-specific updates (documentation, SNAPSHOT removal, etc). In
    particular new functionality does not go here unless it is a
    solution to a JIRA report targeted at the release.

*   Normal development continues on the trunk.

### Dependencies and branches

*   The trunk should always be "cutting edge" and as such should
    usually be pointing at SNAPSHOT versions of all dependencies. This
    allows for continuous integration with our partner projects.

*   Soon after a release branch is cut, the RM is responsible for
    removing ALL dependencies on SNAPSHOT versions and replacing them
    with officially released versions. This change happens only on the
    release branch.

### Managing change and issue resolution with a release branch

*   The RM goes through JIRA issues and sets "fix for" to point to
    both "NIGHTLY" and the new branched release number for the fixes
    that are targeted for the release after the branch is cut.

*   In general, the assignee/coder fixes JIRA issues or makes other
    changes *on the trunk*. If the JIRA issue is targeted at the
    release, or upon coder's discretion, they then merge the fix over
    to the release branch.

*   This way the trunk is ALWAYS up-to-date, and we don't have to
    worry about losing fixes that have only been made on the release
    branch.

*   When the assignee resolves an issue, they confirm it's been
    fixed in both branches, if appropriate.

### Checking changes into the branch

*   If bug fixes are needed later for a release which has long
    since happened (to fix user issues, etc), those fixes generally
    should also happen on the trunk first assuming the problem still
    exists on the trunk.

*   There are only two cases where we would ever check anything
    into the branch without first checking it into the trunk. 1)
    Release specific items (release number references, release notes,
    removal of SNAPSHOTs), and 2) if the trunk has moved on in some
    incompatible way.

Performing a release
--------------------

### Preparation

Verify that the code meets the basic requirements for being releasable:

1.  Check that the set of legal (`legal/*.LICENSE`) files corresponds to the set of third party
    JARs included in the binary distribution.

2.  Check that the `apache-release` profile works correctly and produces the required distributions.
    The profile can be executed as follows:
    
        mvn clean install -Papache-release

You may also execute a dry run of the release process: mvn release:prepare -DdryRun=true. In a dry run, the generated zip files will still be labled as SNAPSHOT. After this, you need to clean up using the following command: mvn release:clean

3. Check that the Maven site can be generated and deployed successfully, and that it has the expected content.

To generate the entire documentation in one place, complete with working inter-module links, execute the site-deploy phase (and check the files under target/staging). A quick and reliable way of doing that is to use the following command: mvn -Dmaven.test.skip=true clean package site-deploy

4.  Check that the source distribution is buildable.

5.  Check that the source tree is buildable with an empty local Maven repository.

If any problems are detected, they should be fixed on the trunk (except for issues specific to the
release branch) and then merged to the release branch.

Next update the release note found under `src/site/markdown/release-notes`. To avoid extra work for
the RM doing the next major release, these changes should be done on the trunk first and then merged
to the release branch.

### Pre-requisites

The following things are required to perform the actual release:

*   A PGP key that conforms to the [requirements for Apache release signing](http://www.apache.org/dev/release-signing.html).
    To make the release process easier, the passphrase for the code signing key should
    be configured in `${user.home}/.m2/settings.xml`:

        <settings>
          ...
          <profiles>
            <profile>
              <id>apache-release</id>
              <properties>
                <gpg.passphrase><!-- key passphrase --></gpg.passphrase>
              </properties>
            </profile>
          </profiles>
          ...
        </settings>

*   The release process uses a Nexus staging repository. Every committer should have access to the corresponding
    staging profile in Nexus. To validate this, login to [repository.apache.org](https://repository.apache.org)
    and check that you can see the `org.apache.axis2` staging profile. The credentials used to deploy to Nexus
    should be added to `settings.xml`:

        <servers>
          ...
          <server>
            <id>apache.releases.https</id>
            <username><!-- ASF username --></username>
            <password><!-- ASF LDAP password --></password>
          </server>
          ...
        </servers>

### Release

In order to prepare the release artifacts for vote, execute the following steps:

If not yet done, export your public key and <a href="https://dist.apache.org/repos/dist/release/axis/axis2/java/core/KEYS"> append it there. </a>

If not yet done, also export your public key to the dev area and <a href="https://dist.apache.org/repos/dist/release/axis/axis2/java/core/KEYS"> append it there. </a>

The command to export a public key is as follows:

<code>gpg --armor --export key_id</code>

If you have multiple keys, you can define a ~/.gnupg/gpg.conf file for a default. Note that while 'gpg --list-keys' will show your public keys, using maven-release-plugin with the command 'release:perform' below requires 'gpg --list-secret-keys' to have a valid entry that matches your public key, in order to create 'asc' files that are used to verify the release artifcats. 'release:prepare' creates the sha512 checksum files.

1.  Start the release process using the following command - use 'mvn release:rollback' to undo and be aware that in the main pom.xml there is an apache parent that defines some plugin versions<a href="https://maven.apache.org/pom/asf/"> documented here. </a>

        mvn release:prepare

    When asked for a tag name, accept the default value (in the following format: `vX.Y.Z`).

2.  Perform the release using the following command - though be aware you cannot rollback as shown above after that. That may need to happen if there are site problems further below. To start over, use 'git reset --hard last-hash-before-release-started' , then 'git push --delete origin vX.Y.Z':

        mvn release:perform

        The created artifacts i.e. zip files can be checked with, for example, 'sha512sum axis2-2.0.0-bin.zip' which should match the generated axis2-2.0.0-bin.zip.sha512 file. In that example, use 'gpg --verify axis2-2.0.0-bin.zip.asc axis2-2.0.0-bin.zip' to verify the artifacts were signed correctly.


3.  Login to Nexus and close the staging repository. For more details about this step, see
    [here](https://maven.apache.org/developers/release/maven-project-release-procedure.html) and [here](https://infra.apache.org/publishing-maven-artifacts.html#promote).

4.  Execute the `target/checkout/etc/dist.py` script to upload the source and binary distributions to the development area of the <a href="https://dist.apache.org/repos/dist/"> repository. </a>

5.  Create a staging area for the Maven site:

        git clone https://gitbox.apache.org/repos/asf/axis-site.git
        cd axis-site
        cp -r axis2/java/core/ axis2/java/core-staging
        git add  axis2/java/core-staging
        git commit -am "create core-staging dir as a prerequisite for the publish-scm plugin"
        git push

6.  Change to the `target/checkout` directory and prepare the site using the following commands:

        mvn site-deploy
        mvn scm-publish:publish-scm -Dscmpublish.skipCheckin=true

    Now go to the `target/scmpublish-checkout` directory (relative to `target/checkout`) and check that there are no unexpected changes to the site. Then commit the changes.

    Update: This plugin has a problem with specifying the remote core-staging dir, created above, with the git URL. See https://issues.apache.org/jira/browse/MSITE-1033 . For now, copy the output of the scmpublish-checkout dir listed above to the core-staging dir created earlier in this doc.

    The root dir of axis-site has a .asf.yaml file, referenced here at target/scmpublish-checkout/.asf.yaml, that is  <a href="https://github.com/apache/infrastructure-asfyaml/blob/main/README.md"> documented here. </a>

7.  Start the release vote by sending a mail to `java-dev@axis.apache.org`.
    The mail should mention the following things:

    *   A link to the Nexus staging repository.
    *   A link to the directory containing the distributions
        (<https://dist.apache.org/repos/dist/dev/axis/axis2/java/core/x.y.x/>).
    *   A link to the preview of the Maven site (<http://axis.apache.org/axis2/java/core-staging/>).

If the vote passes, execute the following steps:

1.  Promote the artifacts in the staging repository. See
    [here](https://central.sonatype.org/publish/release/#close-and-drop-or-release-your-staging-repository)
    for detailed instructions for this step.

2.  Publish the distributions:

        svn mv https://dist.apache.org/repos/dist/dev/axis/axis2/java/core/x.y.z \
               https://dist.apache.org/repos/dist/release/axis/axis2/java/core/

3.  Publish the site:

        git clone https://gitbox.apache.org/repos/asf/axis-site.git
        git rm -r core
        git mv core-staging core
        git commit -am "Axis2 X.Y.Z site"
        git push

It may take several hours before everything has been synchronized. Before proceeding, check that

*   the Maven artifacts for the release are available from the Maven central repository;
*   the Maven site has been synchronized;
*   the distributions can be downloaded from the mirror sites.

Once everything is in place, send announcements to `java-user@axis.apache.org` (with copy to
`java-dev@axis.apache.org`) and `announce@apache.org`. Since the two lists have different conventions,
audiences and moderation policies, it is recommended to send the announcement separately to the two lists.
Note that mail to `announce@apache.org` must be sent from an `apache.org` address and will
always be moderated. The announcement sent to `announce@apache.org` also should include a general description
of Axis2, because not everybody subscribed to that list knows about the project.

### Post-release actions

1.  Update the DOAP file (`etc/doap_Axis2.rdf`) and add a new entry for the release.

2.  Update the status of the release version in JIRA.

3.  Remove old (archived) releases from <https://dist.apache.org/repos/dist/release/axis/axis2/java/core/>.

4.  Create an empty release note for the next release under `src/site/markdown/release-notes`.
