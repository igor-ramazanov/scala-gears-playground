{
  inputs = {
    devshell.url = "github:numtide/devshell";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    { devshell
    , nixpkgs
    , flake-utils
    , ...
    }: flake-utils.lib.eachSystem [ "x86_64-linux" ] (system:
    let
      pkgs = import nixpkgs {
        inherit system;
        overlays = [ devshell.overlays.default ];
      };
    in
    {
      formatter = pkgs.alejandra;
      devShell = pkgs.devshell.mkShell {
        name = "scala-gears-playground";
        motd = "Entered scala-gears-playground development environment";
        packages =
          let
            libs = pkgs.lib.flatten (builtins.map
              (e: [
                (pkgs.lib.getDev e)
                (pkgs.lib.getLib e)
              ]) [ pkgs.zlib ]);
          in
          [
            pkgs.clang
            pkgs.llvmPackages.libcxx
          ] ++ libs;
        env = [
          {
            name = "LIBRARY_PATH";
            prefix = "$DEVSHELL_DIR/lib";
          }
          {
            name = "C_INCLUDE_PATH";
            prefix = "$DEVSHELL_DIR/include";
          }
          {
            name = "LLVM_BIN";
            prefix = "${pkgs.clang}/bin";
          }
        ];
      };
    });
}
