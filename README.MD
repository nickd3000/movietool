# MovieTool

A Java based application for scanning movie file collections.
A [TMDB API key](https://developers.themoviedb.org/3/getting-started/introduction) is required to use this.

### Features
- Scan your library and retrieve movie details from [TMDB](https://www.themoviedb.org).
- Show duplicated files.
- Show incorrectly named files.
- Show list of movie collections and what's missing.
- Show list of popular movies by year, and indicate movies you don't own.

### Expectations
- All movie files in single folder
- Movie naming format: The Mask (1994).mp4
- Movie file names should contain release year.

### Config
The following variables should be set up and changed to match your folder locations etc:
```
export MOVIETOOL_DATAFILEPATH=~/movietool.json
export MOVIETOOL_MOVIEFOLDERPATH=/Volumes/PLEX_4TB/Movies
export MOVIETOOL_ALLOWEDFILETYPES=m4v,avi,mkv,mp4
export MOVIETOOL_TMDBAPIKEY={big-ass code from TMDB}
```
MOVIETOOL_DATAFILEPATH - Set this to where you want the data cache to live
MOVIETOOL_MOVIEFOLDERPATH - Set this to the location of your movie files
MOVIETOOL_ALLOWEDFILETYPES - List of allowed movie file types
MOVIETOOL_TMDBAPIKEY - Your own personal TMDB API key
