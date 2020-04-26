PROJECT_NAME=S3Util
PROJECT_VERSION=1.0-SNAPSHOT
JAR:=target/$(PROJECT_NAME)-$(PROJECT_VERSION).jar
JAR_WITH_DEPENDENCIES:=target/$(PROJECT_NAME)-$(PROJECT_VERSION)-jar-with-dependencies.jar
APP_MAIN_CLASS:=$(PROJECT_NAME)
FILE_TO_UPLOAD?=$(CURDIR)/testfile.txt

BUCKET_NAME=
ROLE_TO_ASSUME_NAME=

-include ./lib/help.mk

all: validate-args clean $(JAR) run

run: validate-args $(JAR) $(FILE_TO_UPLOAD) ## Build and execute the program e.g. AWS_PROFILE=some make run FILE_TO_UPLOAD=/my/file/path BUCKET_NAME=my-bucket-name ROLE_TO_ASSUME_NAME=my-role
	mvn exec:java \
		-Dexec.args="$(FILE_TO_UPLOAD) $(BUCKET_NAME) $(ROLE_TO_ASSUME_NAME)" \
		-Dexec.commandLineArgs="$(BUCKET_NAME)" \
		-Dexec.mainClass="$(APP_MAIN_CLASS)" \
		-Dexec.cleanupDaemonThreads=false

#nb: To use, use the jar-with-dependencies assembly config in the pom.xml
# Run as standard jar (added config to make jar with deps file, in the target folder)
run-with-deps: validate-args $(JAR) $(FILE_TO_UPLOAD) ## Build and execute the program AWS_PROFILE=some make run-with-deps FILE_TO_UPLOAD=/my/file/path BUCKET_NAME=my-bucket-name ROLE_TO_ASSUME_NAME=my-role
	java -cp $(JAR_WITH_DEPENDENCIES) \
		$(APP_MAIN_CLASS) \
		$(FILE_TO_UPLOAD) \
		$(BUCKET_NAME) \
		$(ROLE_TO_ASSUME_NAME)

$(JAR): pom.xml src/main/java/*.java
	mvn clean install

dump-classpath: ## Show dependencies (classpath)
	mvn dependency:build-classpath \
		| grep -vE "\[.*\]" \
		| grep -v "mvn dependency" \
		| tr ":" "\n"

validate-args:
	$(if $(strip $(BUCKET_NAME)),,$(error BUCKET_NAME required, [$(BUCKET_NAME)] is invalid))

# Upload a dummy file (with just the date in it) if FILE_TO_UPLOAD is not provided
$(FILE_TO_UPLOAD):
	date > $@

clean:
	@mvn clean
	@-rm -rf $(FILE_TO_UPLOAD)