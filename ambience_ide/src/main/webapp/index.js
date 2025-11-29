
function onLoad() {
    document.getElementById('example_1').value = sample_config;
}

var sample_config = `
// Sample music_config.txt

// -------------------------------------
// Playlists
playlist main_menu = [
    "MainMenu.mp3"<+3.0>
];

// -------------------------------------
// Schedule
begin
    play [ "HAHAHA.mp3" ];
end
`;
