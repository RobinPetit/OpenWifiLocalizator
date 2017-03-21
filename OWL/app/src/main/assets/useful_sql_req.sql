-- Get all Plan which have at least one wifi
SELECT DISTINCT n.PlanId AS IdPlan, p.Name AS NamePlan
FROM Wifi w
    JOIN Node n
        ON n.Id = w.NodeId
    JOIN Plan p
        ON n.PlanId = p.Id
ORDER BY IdPlan


-- Update coordinate X of all Node on a plan
UPDATE Node
SET X = X+0
WHERE PlanId = -1


-- Delete all plan execpt which you add
DELETE
FROM Plan
WHERE Id != 1 AND Id != 2 AND Id NOT IN (5)

-- Remove all node which haven't any plan (plan have been deleted)
DELETE
FROM Node 
WHERE NOT EXISTS (
    SELECT *
    FROM Plan p
    WHERE p.Id = PlanId
)

-- Remove all edge which haven't any node (node have been deleted)
DELETE 
FROM Edge 
WHERE NOT EXISTS (
    SELECT *
    FROM Node n
    WHERE Node1Id = n.Id OR Node2Id = n.Id
)

-- Remove all AliasLink which haven't any node (node have been deleted)
DELETE 
FROM AliasesLink
WHERE NOT EXISTS (
    SELECT *
    FROM Node n
    WHERE NodeId = n.Id
)

-- Remove all Alias which haven't any link with a node (node have been deleted)
DELETE
FROM Aliases
WHERE NOT EXISTS (
    SELECT *
    FROM AliasesLink link
    WHERE Id = link.AliasId
)
