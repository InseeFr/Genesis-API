FROM eclipse-temurin:25.0.1_8-jre

ENV PATH_TO_JAR=/opt/app/app.jar
WORKDIR /opt/app/
COPY ./target/*.jar $PATH_TO_JAR

ENV JAVA_TOOL_OPTIONS_DEFAULT \
    -XX:MaxRAMPercentage=75

# Setup a non-root user context (security)
RUN addgroup --gid 1000 tomcatgroup \
 && adduser  --uid 1000 --ingroup tomcatgroup --disabled-password --gecos "" --no-create-home tomcatuser \
 && mkdir -p /opt/app/temp-files \
 && chown -R 1000:1000 /opt/app

USER 1000

ENTRYPOINT [ "/bin/sh", "-c", \
    "export JAVA_TOOL_OPTIONS=\"$JAVA_TOOL_OPTIONS_DEFAULT $JAVA_TOOL_OPTIONS\"; \
    exec java -jar $PATH_TO_JAR" ]