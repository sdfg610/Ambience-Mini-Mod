
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

playlist home = [
    "Home.mp3"
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


// ----------------------------------------
// Server playlists  (Clients can stream this music from the server by use of the $server_playlists property.)
serve_playlists: group
    "area": group
        "town": [ "Town.mp3" ];  // This playlist is accessed using $server_playlists["area.town"]
        "market": [ "Market.mp3" ];
    end

    "dimension": group
        "minecraft:the_nether": [ "ServerNether.ogg" ];
        "minecraft:the_end": [ "ServerEnd.ogg" ];
        "minecraft:overworld": [ "ServerOverworld.ogg" ];  // Not used in schedule below. Clients are free to (not) use server music.
    end
end


// -------------------------------------
// Schedule  (This is what decides how the Minecraft client selects music based on game state.)
begin
    when (@main_menu) play main_menu;
    when (@joining) play joining;

    when (@in_game)
    begin
        interrupt when (@paused) play paused;
        interrupt when (@boss_fight) play boss_battle;

        when ($dimension ~~ "nether") play the_nether ++ $server_playlists["dimension.minecraft:the_nether"];
        when ($dimension ~~ "end") play the_end ++ $server_playlists["dimension.minecraft:the_nether"];
        
        let _area = $areas[0] in
        begin
            when ( _area.name == "Home" && _area.owner == $uuid ) play home;
            when ( _area.name == "Town" ) play $server_playlists["area.town"];
            when ( _area.name == "Market" ) play $server_playlists["area.market"];
        end

        // All other dimensions, including overworld
        when (@day) play day;
        when (@night) play night;
    end
end
`;
