FROM ubuntu:latest
LABEL authors="oshayer"

ENTRYPOINT ["top", "-b"]