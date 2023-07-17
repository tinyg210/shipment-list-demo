FROM hashicorp/terraform:light

WORKDIR /app
COPY /terraform /app

COPY /terraform/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
