MATCH (p:GitPlatform {id: 'GITBUCKET'})
SET p += {id: 'BITBUCKET', name: 'Bitbucket'};