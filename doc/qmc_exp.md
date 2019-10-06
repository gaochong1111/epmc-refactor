## exp syntax    
**Exp**:        
    ExpressionITE    
    
**CompleteProp**:      
    (label ":")? Exp (";")?    
    
**ExpTemporal**:         
    TemporalBinary    
    
**ExpressionITE**:       
    ExpressionImplies ("?" ExpressionImplies ":" ExpressionITE)?    
    
**ExpressionImplies**:       
    ExpressionIff ("->" ExpressionIff)*    
    
**ExpressionIff**:       
    ExpressionOr ( "<=>" ExpressionOr)*    
    
**ExpressionOr**:       
    ExpressionAnd ( "|" ExpressionAnd)*    
    
**ExpressionAnd**:       
    ExpressionNot ( "&" ExpressionNot)*    
    
**ExpressionNot**:       
    "!" ExpressionNot      
    | ExpressionEqNe    
    
**ExpressionEqNe**:         
    ExpressionROp (EqNe ExpressionROp)*    
    
**EqNe**: "=" | "!="    
    
**ExpressionROp**:       
    ExpressionPlusMinus (LtGeLeGe ExpressionPlusMinus)*    
    
**LtGeLeGe**: "<" | "<=" | ">" | ">="    
    
**ExpressionPlusMinus**:       
    ExpressionTimesDivide (PlusMinus ExpressionTimesDivide)*    
    
**PlusMinus**: "+" | "-"    
    
**ExpressionTimesDivide**:      
    ExpressionUnaryMinus ( TimesDivide ExpressionUnaryMinus)*    
    
**TimesDivide**: "*" | "/"    
    
**ExpressionUnaryMinus**:    
    "-" ExpressionUnaryMinus       
    | ExpressionTranspose    
    
**ExpressionTranspose**:       
    Basic ("'")?    
    
**Basic**:       
    Imaginary    
    | Boolean    
    | Function    
    | Identifier    
    | Int    
    | Real    
    | Matrix    
    | ProbQuant    
    | RewQuant    
    | SteadyQuant    
    | SuperOperator    
    | Parenth    
    | Label    
    | Filter    
    | BraKet    
    
**BraKet**:    
    ("<" IdOrInt "|" ( IdOrInt ">")? "\_" IdOrInt         
    | "|" IdOrInt ">" "\_" IdOrInt)      
    ("<" IdOrInt "|" "\_" IdOrInt        
    | "|" IdOrInt ">" "\_" IdOrInt)?      
    
**修改意见**：    
    
**Ket**:    
    "|" ("0" | "1") ">" ("\_" INT)   
    | "|" INT ">" "\_" INT    
    | "|" Identifier ">"    
    
**Bra**:    
    "<" ("0" | "1") "|" ("\_" INT)   
    | "<" INT "|" "\_" INT    
    | "<" Identifier "|"       
    
**BraKet**:    
    ("<" IdOrInt "|" IdOrInt ">")    
    | Ket (Bra | Ket)?    
    | Bra (Bra)?    
        
**IdOrInt**: INT | Identifier    
    
**Function**:      
    SpecialFunction    
    | FunctionMultipleParams "(" Function2 ")"      
    | FunctionOneParam "(" Function1 ")"      
    | "func" "("     
        ( Sqrt     
        | FunctionMultipleParams "," FunctionN    
        | FunctionTwoParams "," Function2    
        | FunctionOneParam "," Function1 )     
        ")"    
    
**SpecialFunction**:       
     Sqrt | Ctran    
    
**Sqrt**: "sqrt" "(" Exp ")"    
    
**Ctran**: "ctran" "(" Exp ")"    
    
**FunctionN**: Exp ("," Exp)+    
    
**Function2**: Exp "," Exp    
    
**Function1**: Exp    
    
**FunctionMultipleParams**: "max" | "min"    
    
**FunctionOneParam**: "floor" | "ceil" | "tran" | "conj"    
    
**FunctionTwoParams**: "pow" | "mod" | "log" | "qeval" | "qprob"    
    
**Parenth**: "(" ExpTemporal ")"    
    
**Identifier**: ID    
    
**Label**: QUOTE (Identifier | "init") QUOTE    
    
**Real**: NUM_REAL    
    
**Imaginary**: IMAG    
    | NUM_REAL IMAG    
    
**Int**: NUM_INT    
    
**SuperOperator**: SUPERATOR_OPEN ExpList SUPERATOR_CLOSE    
    | "mf2so" "(" Exp ")"    
    
**ExpList**: Exp ( "," Exp)*    
    
**Vector**: "{" ExpList "}"    
    
**Boolean**: "true" | "false"    
    
**Matrix**: ("Identity" | "ID") "(" Exp ")"    
    | ( "Paulix" | "PX")    
    | ( "Pauliy" | "PY")    
    | ( "Pauliz" | "PZ")    
    | ( "Hadamard" | "HD")    
    | ( "CNOT" | "CN")    
    | "M0"    
    | "M1"    
    | ("PhaserShift" | "PS") "(" Exp ")"    
    | ( "Swap" | "SW")    
    | ( "Toffoli" | "TF")    
    | ( "Fredkin" | "FK")    
    | SingleMatrix    
    
**修改意见**：    
    M0,M1 扩展为 M(INT, INT)    
    
    
**SingleMatrix**: "{" MatrixRow (";" MatrixRow)* "}"    
    
**MatrixRow**: Exp ( "," Exp)*    
    
**OldSchoolFilter**: "{" ExpTemporal "}"     
    ("{" ( "max" "}" | "min" "}" ("{" "max" "}")?) )?    
    
**ProbQuant**:    
    PropQuantProbDirType     
    (    
        ("=" ("?" | ExpTemporal) "[" ExpTemporal ("given" ExpTemporal)? (OldSchoolFilter)? "]")    
    | PropQuantCmpType ExpTemporal "[" ExpTemporal ("given" ExpTemporal)? (OldSchoolFilter)? "]"    
    )    
    
**SteadyQuant**:    
    SteadyQuantProbDirType     
    (    
        ("=" ("?" | ExpTemporal) "[" ExpTemporal ("given" ExpTemporal)? (OldSchoolFilter)? "]")    
    | PropQuantCmpType ExpTemporal "[" ExpTemporal ("given" ExpTemporal)? (OldSchoolFilter)? "]"    
    )    
    
**PropQuantCmpType**: "<=" | "<" | ">=" | ">"    
    
**RewardPath**:     
    "F" ExpTemporal    
    | "C" ("<=" ExpTemporal)? ("," "DISCOUNT" "=" ExpTemporal)?    
    | "I" "=" ExpTemporal    
    | "S"    
    
**RewardStructure**:     
    "{" (QUOTE Identifier QUOTE | Exp) "}"    
    
**RewQuant**:     
    (    
        "R" (RewardStructure)? ("min" | "max")?    
        | "Rmin"    
        | "Rmax"    
    )    
    (    
        "=" ("?" | ExpTemporal)    
        | PropQuantCmpType ExpTemporal    
    )    
    "[" RewardPath ("given" ExpTemporal)? (OldSchoolFilter)? "]"    
    
**PropQuantProbDirType**: "P" | "Pmax" | "Pmin" | "Q" | "Qmax" | "Qmin"    
    
**SteadyQuantProbDirType**: "S" | "Smax" | "Smin"    
    
**ExpressionFilterType**: "min" | "max" | "+" | "&" | "|" | Identifier    
    
-Identifier支持-: count | sum | avg | first | range | forall | exists | state | argmin | argmax | print | printall    
    
**Filter**:     
    "filter" "(" ExpressionFilterType "," ExpTemporal ( "," ExpTemporal)? ")"    
    
**TimeBound**:     
    (    
        "<" Exp    
        | "<=" Exp    
        | ">" Exp    
        | ">=" Exp    
        | ("["|"]") Exp "," Exp ("]" | "[")    
    )?    
    
**TemporalBinary**:     
    TemporalUnary (TempBinType TimeBound "(" TemporalBinary ")")?    
    
**TempBinType**: "W" | "R" | "U"    
    
**TemporalUnary**:     
    TempUnType TimeBound "(" TemporalUnary ")"    
    | ExpressionITE    
    
**TempUnType**: "X" | "F" | "G"    
    
    
    
    
# 结束语    
按照prism中5个运算符的结合方式及优先级重新核对语法     
    
http://www.prismmodelchecker.org/manual/ThePRISMLanguage/AllOnOnePage    
     
     
     
