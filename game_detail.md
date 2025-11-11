# Game Detail
## Endpoint
/game/{gameId}

## The data returned
(For all the data required below, unless otherwise noted, all things should be retrieved with repect to the date and season of the game under consideration.  
So for example statistics retrieved, and records calculated should be as of the previous date, to recognize what ones state of knowledge would have been prior to the game starting.  In many cases either early in the season, or for future games, remember that many of the data fields might be null.)

For the **game**
* the date
* the conference name and logo (if it is a conference game)
* the head-to-head record for the season in the form of homeTeamHtHWins, awayTeamHtHWins
* A list of the last five meetings of both teams.  This may reach into prior seasons.  There also may be fewer than five.  Each game listed should contain:
  * the date
  * the home team name
  * the home team abbreviation
  * the home team score
  * the away team name
  * the away team abbreviation
  * the away team score
* The point spread if available
* The over/under if available
* The home and away money lines if available
* A theoretical value for the point spread.  The methodology should be left as a stub fo be finished later.
* A theoretical value for the over/under.  The methodology should be left as a stub fo be finished later.
* A predicted percentage chance to win for either team.  The methodology should be left as a stub fo be finished later.
* Theoretical values for the home and away money lines.  The methodology should be left as a stub fo be finished later.

For each of the home and away **teams**
* the id
* the name
* the abbreviation
* the logo
* the conference (name and logo)
* score (if game is complete)
* the overall win-loss record
* the conference win-loss record
* the home win-loss record
* the away win-loss record
* the neutral site win-loss record
* the win-loss record over the last five games
* the current winning or losing streak
* the mean and std deviation of points for per game (available as statistic POINTS_FOR_AVG, POINTS_FOR_SD)
* the mean and std deviation of points against per game (avaiable as statistic POINTS_AGAINST_AVG, POINTS_AGAINST_SD
* the correlation between points for and points against (available as statistic PTS_FOR_PTS_AGAINST_COV)
* the linear regression power value and rank (available as statistic LINEAR_REGRESSION)
* the logistic regression power value and rank (available as statistic LOGISTIC_REGRESSION)
* the RPI value and rank
* Every completed game of the current season, for game listed should contain:
  * the date (available as statistic LOGISTIC_REGRESSION)
  * the opponent id
  * the opponent name
  * the opponent abbreviation
  * the opponent score


## Implementation
Please cretate a new data object called GameDetail.  This will be a data transfer object that will be returned by the endpoint.  It will contain all the data required for the game detail page.
For creating the data, favor small utility methods to handle components of the data objects above.  You can create a new service class or use an existing one at your discretion.