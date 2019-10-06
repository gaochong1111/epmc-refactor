# hoa format syntax

**Automaton**:    
    Header Body

**Header**:    
    "HOA" "v1"     
    (States | AP | Alias | Acceptance | AccName | Tool | Name)*    
**States**:     
    "States:" INT    
**Start**:    
    "Start:" INT    
**AP**:    
    "AP:" INT (Quoted)*    
**Acceptance**:    
    "Acceptance" INT AcceptanceOr    
**AcceptanceOr**:    
    AcceptanceAnd ( "|" AcceptanceAnd)*    
**AcceptanceAnd**:    
    ( AcceptanceSet | AcceptanceBoolean )     
        ("&" (AcceptanceSet|AcceptanceBoolean) AcceptanceAnd)*    
**AcceptanceSet**:    
    ("Inf" | "Fin") "(" ("!")? INT ")"     
**AcceptanceBoolean**:    
    "t" | "f"    
**AccName**:    
    "acc-name:"     
        ("Buchi"     
        | "generalized-Buchi" INT     
        | "co-Buchi"     
        | "generalized-co-Buchi" INT    
        | "Streett" INT     
        | "Rabin" INT     
        | "generalized-Rabin" INT (INT)*     
        | "parity" INT ("min" | max) (odd | even) INT    
        | "all"    
        | "none"    
        | ID (ID | INT)*    
        )    
**Alias**:    
    "Alias" Aname ExpressionOr    
**Tool**:    
    "tool:" Quoted (Quoted)?    
**Name**:    
   "name:" Quoted    
**Properties**:    
    "properties:" ( ID )*    

**Body**:    
    "--BODY--"    
    (State)*    
    "--END--"    
**State**:    
    "State:" ("[" Guard "]")?  INT (Quoted)? AcceptanceSets    
    ( ("[" Guard "]")? INT AcceptanceSets )*    
**AcceptanceSets**:    
    ("{" (  INT )* "}")?     
**Guard**:    
    ExpressionOr    
**ExpressionOr**:    
    ExpressionAnd ( "|" ExpressionAnd)*    
**ExpressionAnd**:    
    ExpressionNot ( "&" ExpressionNot)*    
**ExpressionNot**:    
    ( "!" ExpressionNot    
        | "(" ExpressionOr ")"    
        | ExpressionTrue    
        | ExpressionFalse    
        | ExpressionIdentifier    
    )    
**ExpressionTrue**:    
    "t"    
**ExpressionFalse**:    
    "f"    
**ExpressionIdentifier**:    
    INT     
    | Aname    
**Quoted**:    
    "QUOTED" // 带引号字符串    
**Aname**:    
    "Aname" //@字符串     

