ai/
├── _common/                  # 不动产：规范文档
│   ├── api_index.txt         # API 白名单 (关键！帮助AI引用现有代码)
│   └── ...
│
├── features/
│   ├── 001_complicated_sys/  # 复杂功能 (标准模式)
│   │   ├── 01_req.md         # 需求
│   │   ├── 02_biz_spec.md    # 业务规则
│   │   ├── 03_design/        # 设计文件 (拆分)
│   │   │   ├── config_struct.md
│   │   │   ├── protocol_def.md
│   │   │   └── logic_flow.md # 这里的逻辑必须是“指令级”的
│   │   └── 04_test_plan.md   # 测试点
│   │
│   ├── 002_simple_feature/   # 简单功能 (快速模式)
│   │   ├── 01_req.md
│   │   └── 02_design_full.md # 所有设计、协议、逻辑在一个文件
│   │
│   └── active_task.txt       # 指示当前正在做哪个任务