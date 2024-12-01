document.addEventListener('DOMContentLoaded', function () {
    const socket = new WebSocket('ws://' + window.location.host + '/ws');
    const searchForm = document.getElementById('search-form');
    const searchQuery = document.getElementById('search-input');
    const searchResults = document.getElementById('searchResults');
    const statusElement = document.getElementById("status");

    // WebSocket event handlers
    socket.onopen = () => {
        console.log('WebSocket connection established');
        statusElement.innerHTML = "Connected";
    };

    socket.onclose = () => {
        console.log('WebSocket connection closed');
        statusElement.innerHTML = "Disconnected";
    };

    socket.onerror = (event) => {
        console.error('WebSocket error: ', event);
        statusElement.innerHTML = "WebSocket error.";
    };

    socket.onmessage = (event) => {
        console.log('Message received from server:', event.data);

        try {
            const data = JSON.parse(event.data);
            console.log('Parsed data:', data);

            if (data.error) {
                console.error('Server error:', data.error);
                const errorElement = document.createElement('p');
                errorElement.textContent = `Error: ${data.error}`;
                errorElement.style.color = 'red';
                searchResults.prepend(errorElement);
            } else if (data.videos && Array.isArray(data.videos)) {
                appendSearchResults(data.searchTerm, data.videos, data.averageGradeLevel, data.averageReadingEase, data.sentiment);
            } else {
                console.error('Unexpected data format:', data);
            }
        } catch (error) {
            console.error('Failed to parse server message as JSON:', error);
        }
    };

    // Form submission handler
    searchForm.addEventListener('submit', function (event) {
        event.preventDefault();
        const query = searchQuery.value.trim();
        if (query) {
            console.log('Sending search query:', query);
            socket.send(query);
        } else {
            console.log('Query is empty');
        }
    });

    // Function to append search results to the page
    function appendSearchResults(searchTerm, videos, averageGradeLevel, averageReadingEase, sentiment) {
        if (!videos || !Array.isArray(videos)) {
            console.error('Invalid videos data or videos is not an array:', videos);
            return;
        }

        console.log('Appending videos for search term:', searchTerm);

        // Clear previous results for the current search term
        const existingSearchTermSection = document.querySelector(`.search-term-section[data-search-term="${searchTerm}"]`);
        if (existingSearchTermSection) {
            existingSearchTermSection.remove();
        }

        const searchTermSection = document.createElement('div');
        searchTermSection.classList.add('search-term-section');
        searchTermSection.setAttribute('data-search-term', searchTerm); // Add a unique identifier for the search term

        const searchStats = document.createElement('div');
        searchStats.classList.add('search-stats');
        searchStats.innerHTML = `
            <a href="/word-stats?query=${encodeURIComponent(searchTerm)}" target="_blank">Search term: <strong>${searchTerm}</strong></a>
            <p>Sentiment: <strong>${sentiment}</strong>, Flesch-Kincaid Grade Level Avg: ${averageGradeLevel.toFixed(2)} Flesch Reading Ease Score Avg: ${averageReadingEase.toFixed(2)}</p>
            <a href="/word-stats?query=${encodeURIComponent(searchTerm)}" target="_blank">Word stats</a>
        `;
        searchTermSection.appendChild(searchStats);

        const videoList = document.createElement('ul');
        videoList.classList.add('video-list');

        videos.forEach((video) => {
            const videoItem = document.createElement('li');
            videoItem.classList.add('video-item');
            videoItem.innerHTML = `
                <div class="video-details">
                    <p class="video-title"><strong>Title:</strong> <a href="${video.url}" target="_blank">${video.title}</a></p>
                    <p class="channel-link"><strong>Channel:</strong> <a href="/channel/${video.channelId}" target="_blank">${video.channelTitle}</a></p>
                    <p class="video-description"><strong>Description:</strong> ${video.description || "No description available"}...</p>
                    <p class="video-readability"><strong>Readability:</strong> Grade Level = ${video.fleschKincaidGradeLevel || "N/A"}, Ease Score = ${video.fleschReadingEaseScore || "N/A"}</p>
                </div>
                <div class="video-thumbnail">
                    <img src="${video.thumbnailUrl}" alt="Thumbnail">
                </div>
            `;
            videoList.appendChild(videoItem);
        });

        searchTermSection.appendChild(videoList);

        // Prepend the new search results to the results container
        searchResults.prepend(searchTermSection);
    }
});
