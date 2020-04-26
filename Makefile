PROJECT_NAME=S3Util
JAR:=target/$(PROJECT_NAME)-1.0-SNAPSHOT.jar
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

$(JAR): pom.xml src/main/java/*.java
	mvn clean install

validate-args:
	$(if $(strip $(BUCKET_NAME)),,$(error BUCKET_NAME required, [$(BUCKET_NAME)] is invalid))

# Upload a dummy file (with just the date in it) if FILE_TO_UPLOAD is not provided
$(FILE_TO_UPLOAD):
	date > $@

clean:
	@mvn clean
	@-rm -rf $(FILE_TO_UPLOAD)