SB_VERSION=0.8.8-SNAPSHOT
SB_JAR="$(eval echo ~$USERNAME)/.m2/repository/com/google/sitebricks/sitebricks-cloud/${SB_VERSION}/sitebricks-cloud-${SB_VERSION}.jar"

# Download the latest SB cloud module from sonatype snapshots (if necessary)
if [ ! -f $SB_JAR ] || [ "$1" == "selfupdate" ]; then
  mvn dependency:get -Dartifact=com.google.sitebricks:sitebricks-cloud:$SB_VERSION -DrepoUrl=https://oss.sonatype.org/content/repositories/google-snapshots
fi

# run it!
SB_VERSION=$SB_VERSION java -cp $SB_JAR com.google.sitebricks.cloud.Cloud $*
