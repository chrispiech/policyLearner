The goal is to populate these four folders with the following (in order):

// SQL Dumps
1. Dump of activities (CSV) for that level from the server
2. Dump of the level sources (CSV) for that level from the server

// Compressed Asts
1. "xml" a dir with all the xmls extracted from the level source dump
2. "unique" a dir where all the xml are turned into asts and we only keep the unique ones.
3. "contiguous" a dir with only contiguous asts.

// Compressed trajectories
1. "trajectories" a dir with all the trajectories extracted from the activities dump
2. "uniqueTrajectories" a dir with all the trajectories, with repeated trajectories condensed.

Once you are that point we can talk about the next step...
