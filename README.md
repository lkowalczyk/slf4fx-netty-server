slf4fx-netty-server
===================

SLF4Fx server using Netty instead of Apache Mina.

The original [SLF4Fx](https://code.google.com/p/slf4fx/) is a framework that allows to integrate Flex logging API on the client side with many Java logging frameworks on the server side. Under the hood it uses Apache Mina as the network layer.

This project only implements the server part while employing [Netty](http://netty.io/) instead of Mina and is compatible with the SLF4Fx client 1.12.

The main class, ``io.github.lkowalczyk.slf4fx.SLF4FxServer`` is a drop-in replacement for the original, ``org.room13.slf4fx.SLF4FxServer``.

Changes
-------

0.6.0
* This release is compatible only with Netty 4 (the previous was only compatible with Netty 3.6)
* Changed package name to io.github.lkowalczyk

Requirements:
- JDK 1.5 or newer
- Netty 3.6.6
