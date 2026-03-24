# MovingShapes – OpenGL ES 2.0 Sphere Example

This project is a simple Android OpenGL ES 2.0 sample demonstrating how to:

- Generate a 3D sphere programmatically
- Render it using modern OpenGL ES 2.0 (shader-based pipeline)
- Animate basic shapes (sphere + triangle)

Originally created as a learning exercise, this repository can serve as a minimal reference for anyone starting with OpenGL ES on Android.

---

## 📌 Features

- Procedural sphere generation (no external models)
- Clean stacks / slices geometry
- Rendering using GL_TRIANGLE_STRIP
- Minimal shader setup (vertex + fragment)
- Lightweight and easy to integrate

---

## 🧠 How the Sphere Works

The sphere is generated mathematically using spherical coordinates:

- Stacks → horizontal divisions (latitude)
- Slices → vertical divisions (longitude)

Each band between two stacks is rendered as a triangle strip.

---

## 🚀 Usage

### Create a Sphere

Sphere sphere = new Sphere(context, 1.0f, 24, 24);

### Set Color (optional)

sphere.setColor(0.2f, 0.71f, 0.90f, 1.0f);

### Draw the Sphere

sphere.draw(mvpMatrix);

---

## 🎨 Shaders

Vertex shader:

uniform mat4 uMVPMatrix;
attribute vec4 vPosition;

void main() {
    gl_Position = uMVPMatrix * vPosition;
}

Fragment shader:

precision mediump float;
uniform vec4 vColor;

void main() {
    gl_FragColor = vColor;
}

---

## 📂 Structure

app/src/main/java/.../objects/Sphere.java

---

## 📜 License

Free for educational use.

---

Author: Mauro Ghiglia
