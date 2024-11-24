document.addEventListener('DOMContentLoaded', function() {
    var socket = new WebSocket('ws://' + window.location.host + '/ws');
    var searchForm = document.getElementById('search-form');
    var searchQuery = document.getElementById('search-input');
    var searchResults = document.getElementById('searchResults');

    socket.addEventListener("open", (event) => {
        document.getElementById("status").innerHTML = "connected";
    });

    socket.addEventListener("close", (event) => {
        document.getElementById("status").innerHTML = "Disconnected";
    });

    socket.addEventListener("error", (event) => {
        console.error("WebSocket error: ", event);
        document.getElementById("status").innerHTML = "WebSocket error: ";
    });

    searchForm.addEventListener('submit', function(event) {
        event.preventDefault();
        var query = searchQuery.value.trim();
        if(query) {
            console.log("Sending search query: ", query);
            socket.send(query);
        } else {
            console.log("empty");
        }
    })

    socket.addEventListener("message", (event) => {
        console.log("Ws message received: ", event.data);
        try {
           var data = JSON.parse(event.data);
           displayVideos(data.videos);
        } catch(error) {
            console.error("Failed to parse server message as JSON:", error);
        }
    });

    function displayVideos(data) {
        console.log("Data received: ", data);

        var searchTerm = data.searchTerm;
        var videos = data.videos;

        searchResults.innerHTML = '';

        var searchTermSection = document.createElement('div');
        var searchHeader = document.createElement('h2');

        searchHeader.textContent = `Results for: "${searchTerm}"`;
        searchTermSection.appendChild(searchHeader);

        var listOfVideos = document.createElement('div');

        videos.forEach(function(video) {
            var videoElement = document.createElement('div');

            var videoTitle = document.createElement('h3');
            videoTitle.textContent = video.title;
            videoElement.appendChild(videoTitle);

            var videoDescription = document.createElement('p');
            videoDescription.textContent = video.description;
            videoElement.appendChild(videoDescription);

            var videoLink = document.createElement('a');
            videoLink.href = `https://www.youtube.com/watch?v=${video.id}`;
            videoLink.textContent = "Watch Video";
            videoLink.target = "_blank";
            videoElement.appendChild(videoLink);

            listOfVideos.appendChild(videoElement);
        });
        searchTermsSection.appendChild(listOfVideos);
        searchResults.appendChild(searchTermsSection);
    };
})


