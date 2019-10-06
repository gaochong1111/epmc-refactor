# 符号说明    
()*: 0到多次      
()?: 0或1次      
()+: 1或多次      
    
# 语法描述    
## model syntax
**ModelPRISM**:      
     (     
         ModelType      
        |Constant      
        |Label      
        |Global      
        |Formula      
        |Module      
        |Rewards      
        |Init      
        |System       
    )*     
    
**ModelType**:       
     "mdp"       
    |"nondeterministic"       
    |"dtmc"       
    |"probabilistic"       
    |"qmc"       
    |"ctmc"       
    |"stochastic"       
    |"pta"       
    
**Constant**:      
     ConstantQmc       
     | ( "const"       
         (  ("int"|"double"|"bool" | "rate"|"prob") )       
             Identifier (    
                 ("=" ExpSemicolon ";")     
                 | ";" )     
       )      
    
    
       
**ConstantQmc**:       
    "const" (("vector" "(" INT ")" ("|" Identifier ">") )        
    | ( "matrix" "(" INT "," INT ")" Identifier )        
    | ( "superoperator(" INT ")" Identifier )) "=" ExpSemicolon ";"    
     
 **Label**:      
     "label" "\"" Identifier "\"" "=" ExpSemicolon ";"    
     
 **Global**:      
     "global" VariableDeclaration    
     
 **Formula**:      
     "formula" Identifier "=" ExpSemicolon ";"    
     
 **Module**:      
     "module"     
         Identifier ( ( "=" ModuleRename ) | ModuleContent )     
     "endmodule"    
     
 **ModuleRename**:      
     Identifier "[" Renaming ("," Renaming)* "]"    
     
 **Renaming**:      
     Identifier "=" Identifier    
     
 **ModuleContent**:      
     ( (VariableDeclaration)*     
         (InvariantDeclaration)? (GuardedCommandDeclaration)* )    
     
 **VariableDelaration**:      
     Identifier ":" Variable ( ("init" ExpSemicolon ";") | (";") )    
     
 **Variable**:      
     ( "bool"     
     | ("[" ExpSepinterval ".." ExpBrack "]")     
     | "clock" )    
     
 **InvariantDeclaration**:      
     "invariant" ExpInvariant "endinvariant"    
     
 **GuardedCommandDeclaration**:      
     "[" (Identifier)? "]" Condition "->" Update ";"    
     
 **Update**:      
     DetUpdate     
     | ProbUpdate    
     
 **ProbUpdate**:      
     ("~[]" | "(" | Identifier | "true")    
        ExpColon ":" DetUpdate     
         ( "+" ExpColon ":" DetUpdate)*    
     
 **DetUpdate**:      
     ( ("true")     
     | ( "(" Identifier "'" "=" ExpParent ")"     
         ( "&" "(" Identifier "'" "=" ExpParent ")")*     
       )     
     )    
     
 **Rewards**:      
     "rewards" ("\"" Identifier "\"")? ( "[" (Identifier)? "]" )?    
         (    
              ("true" | "false" | Identifier | "(" | "-" | "!" )?     
                  ExpColon ":" ExpSemicolon ";")+     
     "endrewards"    
     
 **Init**:      
     "init" ExpInit "endinit"    
     
 **System**:      
     "system" SystemContent "endsystem"    
     
 **SystemContent**:      
     SystemParallelCommonActions    
     
 **SystemParallelCommonActions**:      
     SystemParallelAsynchronous     
        ("||" SystemParallelSetActions)*    
     
 **SystemParallelAsynchronou**:      
     SystemParallelSetActions     
        ("|||" SystemParallelCommonActions)*    
     
 **SystemParallelSetActions**:      
     SystemRenHid     
         ( "|" "[" IdSet "]" "|" SystemRenHid )?    
     
 **SystemRenHid**:      
     SystemBase     
         (   ( "{" IdSet "}" )     
           | ( "{" RenameMap "}" ) )*    
     
 **RenameMap**:      
     ExpressionIdentifier "<->" ExpressionIdentifier     
         ( "," ExpressionIdentifier "<->" ExpressionIdentifier )*    
     
 **IdSet**:      
     ExpressionIdentifier ( "," ExpressionIdentifier )*    
     
 **SystemBase**:      
     ( ExpressionIdentifier )     
     | ( "(" SystemParallelCommonActions ")")    
    
    

