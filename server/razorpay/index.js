/*
Secure Razorpay sample server with endpoints:
- GET /config -> returns public key id (no secret)
- POST /create-order -> creates Razorpay order, returns order json
- POST /verify -> verifies payment signature sent from client after payment success
Set env variables: RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET
*/
const express = require('express');
const Razorpay = require('razorpay');
const bodyParser = require('body-parser');
const crypto = require('crypto');
const app = express();
app.use(bodyParser.json());

const KEY_ID = process.env.RAZORPAY_KEY_ID || 'REPLACE_KEY_ID';
const KEY_SECRET = process.env.RAZORPAY_KEY_SECRET || 'REPLACE_KEY_SECRET';

const razorpay = new Razorpay({ key_id: KEY_ID, key_secret: KEY_SECRET });

app.get('/config', (req, res) => {
  res.json({ key_id: KEY_ID });
});

app.post('/create-order', async (req, res) => {
  try {
    const { amount, currency, receipt } = req.body;
    if (!amount) return res.status(400).json({ error: 'amount required in paise' });
    const order = await razorpay.orders.create({
      amount: amount,
      currency: currency || 'INR',
      receipt: receipt || 'rcptid_11'
    });
    res.json(order);
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: e.message });
  }
});

// Verify payment signature after checkout (client should call this with payload)
app.post('/verify', (req, res) => {
  try {
    const { razorpay_order_id, razorpay_payment_id, razorpay_signature } = req.body;
    const body = razorpay_order_id + "|" + razorpay_payment_id;
    const expectedSignature = crypto.createHmac('sha256', KEY_SECRET).update(body.toString()).digest('hex');
    if (expectedSignature === razorpay_signature) {
      // signature valid - update order doc or mark payment success in DB as needed
      return res.json({ ok: true });
    } else {
      return res.status(400).json({ ok: false, error: 'invalid signature' });
    }
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: e.message });
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log('Razorpay server listening on', port));