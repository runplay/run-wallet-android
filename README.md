# run wallet
![alt tag](http://iota.runplay.com/img/icon.png "run IOTA wallet Logo")

```

WARNING !
This repository is now out of date a no longer worked on, only use for reference, an update to the latest IOTA library is required for it to work

```
  
<b>run wallet</b> provides a fast, efficient and easy to manage Wallet for the IOTA currency
<br/>

<h2>Features</h2>

Full of features<br/>
✅ Send and receive IOTA's<br/>
✅ <b>Multi Seed Wallet</b> 🌿<br/>
✅ <b>Multi Address Transfers</b> 🌿<br/>
✅ Automatic Seed balance discovery<br/>
✅ Automatic Address generating<br/>
✅ Clear detailed Address balance and Pending balance data<br/>
✅ Address <b>Pigs</b> 🐷 <b>sticky addresses</b> 🐷 keep any address from being used 🐷<br/>
✅ <b>TOR</b> enable private IOTOR (IOTA over TOR)<br/>
✅ Customisable, different colour themes to style the app<br/>
✅ Transaction and address history<br/>
✅ Currency data and Charts 💵 💴 💶 💷<br/>
✅ Full Node control<br/>
✅ Advanced configuration allowed, min weight and Address security<br/>
✅ Simplified or Detailed layouts<br/>
✅ QR Code support<br/>
<br/><br/>
Code originally based on the IOTA foundation wallet
https://github.com/iotaledger/android-wallet-app
<br/>
<br/>
<h2>Updates</h2>
v1.3.8750 - Rebranded to run wallet as due to Google Play terms copyright infringements on logo and name usage<br/>
v1.3.8650 - Security update, big issue found around using an old IOTA library, all previous release of run IOTA wallet should be discarded if you have.<br/>
The whole release-0.9.10 of jota is included in this release, like the previous library that had the security bug in it as it is not possible to overwrite the current library to include the wereAddressSpentFrom feature which is also a security issue not being in the official GitHub JOTA library, runplay had previous provided the code for this to issue #101<br/><br/><br/>
v1.3.8500 - Pre snaphot used address checker, decimal place on > iota values, Network area improvements<br/>
v1.3.8400 - Solutions for issues: #5 & #6<br/>
v1.3.8350 - Print paper wallet added with trinity wallet compatibility, import QR Seed & trinity wallet compatibility and some bug fixes, promte transaction improvements<br/>
v1.3.8150 - QR Reader improvements (accepts raw values and json), Deep linking now supported (iota://address?value=100&message=SOME9MESSAGE&tag=SOME9TAG), plus a couple bug fixes.<br/>
v1.3.8100 - Used Address balance Auto & manual check added, Fast wallet Switcher, Ui tweak for large seed values, Reload Seed request (performs a first load again), Background Service performance improvements and a couple Bug fixes.<br/>
v1.3.8000 - Improved Seed generator, improved Promotes, field.carriota node added to node list options, improved wallet load display, Active wallet name displayed in Toolbar, improved nudging (faster and more often)<br/>
v1.3.7900 - Edit Wallet section created, Added one extra Theme, Rate App prompt added<br/>
v1.3.7800 - Bug fixes identified in Google Play console<br/>
v1.3.7700 - Welcome section added and opened the app to full app browsing without Seed<br/>
v1.3.6600 - Colour Themes, 10 different app colours styles to choose from<br/>
v1.3.5500 - Multi Address transfer and TOR ability<br/>
<br/><br/>
<h2>Deep linking</h2>
App supports deep linking in the following way:<br/>
<b>iota://</b> or <b>runiota://</b><br/><br/>
<b>format</b>: iota://address?amount=100&message=PARAM9MESSAGE&tag=PARAM9TAG<br/><br/>
<b>amount</b>: Must be in raw IOTA value (long, BigNumber)<br/>
<b>address</b>: Valid IOTA address<br/>
<b>message</b>: A-Za-z 9, other characters will be removed, whitespace converted to '9'<br/>
<b>tag</b>: A-Za-z 9, other characters will be removed, whitespace converted to '9'<br/>

<br/><br/>
<h2>Download</h2>

<a href="https://play.google.com/store/apps/details?id=run.wallet.android"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" width="323" height="125"/></a>

<h2>How to build</h2>

```bash
$ git clone https://github.com/runplay/run-wallet-android
$ cd run-wallet-android
$ ./gradlew clean build
```

Available only on Github, Google Play and Amazon market, do not download run IOTA wallet anywhere else


<h2>Support the project</h2>
If you find the app useful, donations are accepted here

IOTA: 9PPMLVNEGQEZLCKTPDSCMKNKNDPMHUTC9PMOAKHGNGOVVTXOTRA99JFPVAAXHPUM99DGLUHOYLMWOL9YCSGRZJIYSW



<h2>Screens</h2>
Here are some screenshots of the app

![alt tag](http://iota.runplay.com/img/gp-tablet1s.png "Promotion image")

![alt tag](http://iota.runplay.com/img/wallet-screen2s.png "Transfers screenshot detailing all actions and status")

![alt tag](http://iota.runplay.com/img/wallet-screen1s.png "Addresses screenshot with balances and pending balances")

![alt tag](http://iota.runplay.com/img/wallet-screen3s.png "All wallets (seed) screenshot")

![alt tag](http://iota.runplay.com/img/wallet-screen4s.png "Send new transfer screenshot")
