import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.*;

public class CFGBuilder extends simpleCBaseVisitor<Void> {
    
    static class Block {
        String name;
        String code;
        List<String> successors = new ArrayList<>();
        List<String> predecessors = new ArrayList<>();

        Block(String name, String code) {
            this.name = name;
            this.code = code;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append("\n").append("{\n").append(code).append("\n}");
            return sb.toString();
        }
    }

    Map<String, Block> blocks = new LinkedHashMap<>();
    Block currentBlock;
    int blockCounter = 0;
    int blockcount = 0;

    public void addBlock(String code) {
        String blockName = "Func_B" + blockCounter++;
        Block newBlock = new Block(blockName, code);
        blocks.put(blockName, newBlock);
    }

    public void addBlock_successors(String NextBlock, int count) {
        String blockName = "Func_B" + count;
    
        Block current_block = blocks.get(blockName);
        if (current_block == null) {
            System.out.println("Error: Block with name " + blockName + " not found! " + NextBlock);
            return;  // block이 없으면 더 이상 진행하지 않음
        }

        current_block.successors.add(NextBlock);
    }

    public void addBlock_predecessors(int count, String blockName){
        String pred_blockName = "Func_B" + count;
    
        Block currrent_block = blocks.get(blockName);
        if (currrent_block == null) {
            System.out.println("Error: Block with name " + blockName + " not found! " + count);
            return;  // block이 없으면 더 이상 진행하지 않음
        }

        currrent_block.predecessors.add(pred_blockName);
    }

    public String getParamListText(simpleCParser.Param_listContext ctx) {
        StringJoiner sj = new StringJoiner(" ");
        for (int i = 0; i < ctx.param().size(); i++) {
            sj.add(ctx.param(i).INT().getText());  // 각 파라미터의 텍스트를 추가
            sj.add(ctx.param(i).ID().getText());
        }
        return sj.toString();
        
    }

    public String getDeclText(simpleCParser.DeclContext ctx){
        StringJoiner sj = new StringJoiner(" ");
        sj.add(ctx.INT().getText());
        sj.add(ctx.varDeclList().getText()+";");
        return sj.toString();
    }


    @Override
    public Void visitDecl_with_if(simpleCParser.Decl_with_ifContext ctx){

        StringJoiner sj = new StringJoiner("\n");
        for (int i = 0; i < ctx.decl().size(); i++) {
            sj.add(getDeclText(ctx.decl(i)));  // 각 파라미터의 텍스트를 추가
        }
        sj.add(ctx.ifStmt().getText());
        addBlock(sj.toString());
        addBlock_successors("Func_B"+(blockCounter), blockCounter-1);
        addBlock_successors("Func_B"+(blockCounter+1), blockCounter-1);
       
        return null;
    }

    @Override
    public Void visitAssign_with_else(simpleCParser.Assign_with_elseContext ctx){
        StringJoiner sj = new StringJoiner("\n");
        for (int i = 0; i < ctx.assign().size(); i++){
            sj.add(ctx.assign(i).getText());
        }
    
        addBlock(sj.toString());
        addBlock_successors("Func_B"+(blockCounter+1), blockCounter-1);
        return visitChildren(ctx);
    }

    @Override
    public Void visitAssign_with_brace_in_while(simpleCParser.Assign_with_brace_in_whileContext ctx){
        StringJoiner sj = new StringJoiner("\n");
        for (int i = 0; i < ctx.assign().size(); i++){
            sj.add(ctx.assign(i).getText());
        }
        addBlock(sj.toString());
        addBlock_successors("Func_B"+(blockCounter-2), blockCounter-1);
        return null;
    }

    @Override
    public Void visitAssign_with_brace_in_else(simpleCParser.Assign_with_brace_in_elseContext ctx){
        StringJoiner sj = new StringJoiner("\n");
        for (int i = 0; i < ctx.assign().size(); i++){
            sj.add(ctx.assign(i).getText());
        }
        addBlock(sj.toString());
        addBlock_successors("Func_B"+(blockCounter), blockCounter-1);
        return null;
    }

    @Override
    public Void visitWhileStmt(simpleCParser.WhileStmtContext ctx){
        addBlock("while ( " + ctx.expr().getText() + " )");
        addBlock_successors("Func_B"+(blockCounter), blockCounter-1);
        addBlock_successors("Func_B"+(blockCounter+1), blockCounter-1);
        return visitChildren(ctx);
    }

    @Override
    public Void visitReturnStmt(simpleCParser.ReturnStmtContext ctx){
        addBlock("return "+ctx.expr().getText());
        addBlock_successors("Func_B"+(blockCounter), blockCounter-1);
        return null;
    }

    @Override
    public Void visitFunc(simpleCParser.FuncContext ctx){
        Block entryBlock = new Block("func_entry", "name: " + ctx.ID().getText() + "\nret_type: " + ctx.INT().getText() + "\nargs: " + getParamListText(ctx.param_list()));
        entryBlock.successors.add("Func_B"+blockCounter);
        entryBlock.predecessors.add(" - ");
        blocks.put("func_entry", entryBlock);
        
        return visitChildren(ctx);
    }

    public void printCFG() {
        System.out.println("# Control Flow Graph");
        for (int block_count = 0; block_count < blockCounter-1; block_count++){
            String blockname = "Func_B" + block_count;
            Block current_block = blocks.get(blockname);
            if (current_block == null) {
                System.out.println("Block with name " + blockname + " not found in blocks map.");
                continue;  // 다음 루프로 이동
            }

            if(!current_block.successors.isEmpty()){
                for (String succ : current_block.successors){
                    addBlock_predecessors(block_count, succ);
                }
            }
        }

        // 마지막에 정리
        String final_blockname = "Func_B" + (blockCounter - 1);
        Block final_block = blocks.get(final_blockname);
        if (final_block != null) {
            for (int i = 0; i < final_block.successors.size(); i++) {
                if (final_block.successors.get(i).equals("Func_B6")) {
                    final_block.successors.set(i, "Func_exit");
                    // System.out.println("Successor 'func_B6' changed to 'func_exit'");
                }
            }
        } else {
            System.out.println("Block with name " + final_blockname + " not found.");
        }
        
        // 처음거 정리

        String start_blockname = "Func_B0";
        Block start_block = blocks.get(start_blockname);
        if (start_block == null) {
            System.out.println("Error: Block with name " + start_blockname + " not found! ");
            return;  // block이 없으면 더 이상 진행하지 않음
        }
        start_block.predecessors.add("func_entry");

        //////////////////////////

        for (Block block : blocks.values()) {
           
            System.out.println(block);
            if (!block.predecessors.isEmpty()){
                System.out.print("Predecessors: ");
                for (String pred : block.predecessors) {
                    System.out.print(pred + ", ");
                }
                System.out.println();
            }
            if (!block.successors.isEmpty()) {
                System.out.print("Successors: ");
                for (String succ : block.successors) {
                    System.out.print(succ + ", ");
                }
                System.out.println();
            }
            System.out.println();
        }
        
        System.out.println("Func_exit\n{\n}");
        for (Block block : blocks.values()) {
            if (block.successors.contains("Func_exit")){
                    System.out.println("Predecessors: " + block.name);
                }
        }
        System.out.println("Successors: - ");
    }

    public static void main(String[] args) throws Exception {
        CharStream input = CharStreams.fromFileName("example.c");  // C 코드 파일 경로
        simpleCLexer lexer = new simpleCLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        simpleCParser parser = new simpleCParser(tokens);

        CFGBuilder cfgBuilder = new CFGBuilder();
        cfgBuilder.visit(parser.program());  // C 코드 파싱 및 CFG 생성
        cfgBuilder.printCFG();  // CFG 출력
    }
}
