# Real-time Face Detection with Firebase Vision ML Kit on Android

There are several branches labeled as `Step*` to follow to get a more shallow introduction to the world of face detection.

### Step 1. Scaffolding

This is a simple camera app. Permissions taken care of cleanly, take a photo and display it on the screen.

### Step 2. Simple Photo Face Detection setup

![Some test faces](https://github.com/shredderskelton/facedetectionlab/blob/master/humans.jpg)

![Outline](https://github.com/shredderskelton/facedetectionlab/blob/master/boxes.png)

Here we add the minimum code required to take a photo using a brilliant little library https://github.com/RedApparat/Fotoapparat, process it and display an image with boxes around the faces.

### Step 3. Playing with the Face Data

In this Step, we play around with the data and have a look at some of the possibilities that we have. This is where you can get creative with Face Detection.

#### Face Contour Outline

![Contour](https://github.com/shredderskelton/facedetectionlab/blob/master/contour.png)

Adding a slightly harder level of complexity, we need to get some more detail from our face detector and draw over the image with some slightly more complex arithmetic.
 
#### Face Replacement

![Troll](https://github.com/shredderskelton/facedetectionlab/blob/master/trolls.png)

Replacing the face with a graphic.

#### Face Altering

![Blur](https://github.com/shredderskelton/facedetectionlab/blob/master/blur.png)

Until now we have been _overlaying_ either graphics or bitmaps. But what about actually altering the bitmap itself? Blurring is an example of alteration, so is colour replacement or almost any kind of image processing such as altering saturation or exposure...  

### Step 4. Going Real-time

Once you have the basic idea behind photo processing, that is the face detection part done. To go real time, there are more moving parts, more code and more considerations, but the theory is the same. Take an image, in this case it is a frame, process it, overlay or alter it, out it on the screen.

Much of this code is adapted from the https://github.com/firebase/quickstart-android repository. I have mostly just converted it to Kotlin, removed the unused code, cleaned it up a little and extended it. 

The major blocker to going realtime is avoiding the UI thread. Initially I used FotoApparat's Frame Processor and simply overlaid a view over the top of the camera stream. This is extremely slow because the UI renders the Camera Preview _and_ the overlay in two separate passes. 

The solution (which is supplied by the quickstart) is to use the old Android Camera and feed the preview frames into the Firebase Vision processor directly, do whatever you want to the image, _then_ render it in the background using SurfaceView. SurfaceView is great because it allows you to draw on it from a background thread, which is how we are able to squeeze so much performance out of the system - by doing as much processing as possible in background threads.   

It's not really necessary to fully understand the mechanics of CameraSource, CameraSourcePreviewView and GraphicsView. It's enough to understand how to set them up and focus on the Processors and Graphics... at least until you want to start optimising performance and quality.

# Further reading

https://firebase.google.com/docs/ml-kit/

