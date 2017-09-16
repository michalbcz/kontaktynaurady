FROM play:1.4.2-1.0
#FROM socialmetrix/play:1.4.2-alpine
MAINTAINER https://socialmetrix.com

VOLUME /db

COPY app/ /kontaktynaurady/app
COPY conf/ /kontaktynaurady/conf/
COPY public/ /kontaktynaurady/public/

WORKDIR  /kontaktynaurady

EXPOSE 8080

RUN echo y | echo prod | play id
RUN play dependencies
RUN play precompile

CMD ["play", "run", "--http.port=8080"]