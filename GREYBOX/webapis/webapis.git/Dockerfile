FROM node:latest


COPY . /src

RUN cd /src; rm -rf node_modules; npm install


EXPOSE 9443

WORKDIR /src

CMD ["node", "app.js"]
