# RiceFieldDetection-DroneSystem

<h1>Live Stream and Navigation Drone System</h1>
<p>Making a rice disease classification with LCNN (Light Convolutional Neural Network) and a stream on raspberry pi and accessed via android for future implementation on drone</p>
<ul>
  <li>
    <h2>Raspberry Pi System</h2>
    <p>Raspberry pi equiped with some sensors, Pi camera, and a Python program that have a longitude and latitude calculation system 
      and a live video stream equiped with my own LCNN that has been trained to detect Ricefield or Bareland.</p>
  </li>
  <li>
    <h2>Android System</h2>
    <p>Android Application equiped with the program to receive and show the live video stream from Raspberry Pi while also receive and
    calculate current Raspberry Pi location by processing longitude and latitude data that Android received.</p>
  </li>
</ul>

<h2>Start Raspberry Pi With Bash Code:<h2>
<code>raspivid -o - -t 0 -n -hf -w 800 -h 600 -fps 30| cvlc -vvv stream:///dev/stdin --sout '#rtp{sdp=rtsp://:8000/}' :demux=h264</code>
