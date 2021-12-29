FROM nunopreguica/scc2122-test

WORKDIR /usr/src/artillery

COPY . .

RUN apk add --no-cache bash
RUN npm i --save-dev nodemailer

CMD ["bash", "tests.sh"]
