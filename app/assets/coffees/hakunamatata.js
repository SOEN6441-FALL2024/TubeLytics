document.addEventListener('DOMContentLoaded', function () {
    var socket = new WebSocket('ws://' + window.location.host + '/websocket');

    socket.onopen = function () {
        console.log("WebSocket connection opened");

        // Send JSON message to the server
        var message = {
            type: "connect",
            content: "Hello, server!"
        };
        socket.send(JSON.stringify(message));
    };

    socket.onmessage = function (event) {
        try {
            var data = JSON.parse(event.data);
            console.log("Message from server:", data);
            document.getElementById("message").innerText = "Server says: " + data.content;
        } catch (e) {
            console.error("Failed to parse server message as JSON:", e);
        }
    };

    socket.onclose = function () {
        console.log("WebSocket connection closed");
    };

    socket.onerror = function (error) {
        console.error("WebSocket error:", error);
    };
});
