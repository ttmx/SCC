var nodemailer = require("nodemailer")

var mail = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: 'joao.pa.bordalo@gmail.com',
    pass: 'bfgnduvolysiopec'
  }
});

var mailOptions = {
  from: 'joao.pa.bordalo@gmail.com',
  to: 'j.bordalo@campus.fct.unl.pt',
  subject: 'Artillery',
  text: 'We\'ve got your test results!',
  attachments: 
  [
  {
  	path: 'workload1.html'
  },
  {
  	path: 'workload2.html'
  },
  {
	path: 'workload4.html'
  }, 
  {
  	path: 'workload5.html'
  }
  ]
};

mail.sendMail(mailOptions, function(error, info){
  if (error) {
    console.log(error);
  } else {
    console.log('Email sent: ' + info.response);
  }
});
