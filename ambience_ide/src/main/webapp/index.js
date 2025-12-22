
function loadSampleConfig() {
    editAreaLoader.setValue('example_1', sample_config);
}

var sample_config = `
// Sample music_config.txt

// -------------------------------------
// Playlists
playlist main_menu = [
    "MainMenu1.mp3"<+3.0>,
    "MainMenu2.mp3"
];

playlist joining = [
    "Joining.mp3"
];

playlist boss_battle = [
    "Boss.mp3"
];

playlist paused = [
    "Paused.mp3"
];

playlist the_nether = [
    "Nether1.mp3"<-3.0>,
    "Nether2.mp3"
];

playlist the_end = [
    "End1.mp3",
    "End2.mp3"
];

playlist day = [
    "Day1.mp3",
    "Day2.mp3"
];

playlist night = [
    "Night1.mp3",
    "Night2.mp3"
];

// -------------------------------------
// Schedule
begin
    when (@main_menu) play main_menu;
    when (@joining) play joining;

    when (@in_game)
    begin
        interrupt when (@paused) play paused;
    	interrupt when (@boss_fight) play boss_battle;

        when ($dimension ~~ "nether") play the_nether;
        when ($dimension ~~ "end") play the_end;

        // All other dimensions, including overworld
        when (@day) play day;
        when (@night) play night;
    end
end
`;
