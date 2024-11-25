document.addEventListener('DOMContentLoaded', function() {
    var socket = new WebSocket('ws://' + window.location.host + '/ws');
    var searchForm = document.getElementById('search-form');
    var searchQuery = document.getElementById('search-input');
    var searchResults = document.getElementById('searchResults');
    var statusElement = document.getElementById("status");

    socket.addEventListener("open", () => {
        statusElement.innerHTML = "connected";
        console.log("WebSocket connection established");
    });

    socket.addEventListener("close", () => {
        statusElement.innerHTML = "Disconnected";
        console.log("WebSocket connection closed");
    });

    socket.addEventListener("error", (event) => {
        console.error("WebSocket error: ", event);
        statusElement.innerHTML = "WebSocket error.";
    });

    searchForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const query = searchQuery.value.trim();
        if (query) {
            console.log("Sending search query: ", query);
            socket.send(query);
        } else {
            console.log("Query is empty");
        }
    });

    socket.addEventListener("message", (event) => {
        console.log("WebSocket message received: ", event.data);
        try {
            const data = JSON.parse(event.data);
            console.log("Parsed JSON data: ", data);
            if (data.videos && Array.isArray(data.videos)) {
                appendSearchResults(data.searchTerm, data.videos);
            } else {
                console.error("Data does not have a valid videos property:", data);
            }
        } catch (error) {
            console.error("Failed to parse server message as JSON:", error);
        }
    });

    function appendSearchResults(searchTerm, videos) {
        if (!videos || !Array.isArray(videos)) {
            console.error("Invalid videos data or videos is not an array:", videos);
            return;
        }

        console.log("Appending videos for search term: ", searchTerm);

        const searchTermSection = document.createElement('div');
        searchTermSection.classList.add('search-term-section');

        const searchStats = document.createElement('div');
        searchStats.classList.add('search-stats');
        searchStats.innerHTML = `
            <p>Search term: <strong>${searchTerm}</strong></p>
            <p>Static Placeholder: (Sentiment: :-) , Flesch-Kincaid Grade Level Avg.: ## Flesch Reading Ease Score Avg.: ##)</p>
        `;
        searchTermSection.appendChild(searchStats);

        const videoList = document.createElement('ul');
        videoList.classList.add('video-list');

        videos.forEach((video, index) => {
            const videoItem = document.createElement('li');
            videoItem.classList.add('video-item');
            videoItem.innerHTML = `
                <div class="video-details">
                    <p class="video-title"><strong>Title:</strong> <a href="${video.url}" target="_blank">${video.title}</a></p>
                    <p class="channel-link"><strong>Channel:</strong> <a href="/channel/${video.channelId}" target="_blank">${video.channelTitle}</a></p>
                    <p class="video-description"><strong>Description:</strong> ${video.description || "No description available"}...</p>
                    <p class="video-stats">Static Placeholder for Flesch-Kincaid Grade Level and Tags</p>
                </div>
                <div class="video-thumbnail">
                    <img src="${video.thumbnailUrl}" alt="Thumbnail">
                </div>
            `;
            videoList.appendChild(videoItem);
        });

        searchTermSection.appendChild(videoList);

        searchResults.prepend(searchTermSection);
    }
});
