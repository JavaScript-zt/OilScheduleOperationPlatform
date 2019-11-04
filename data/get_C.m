%% ���㲻ͬ�㷨��cָ��
function [ result ] = get_C()
    % ����·��
    base_path = 'NSGAII/';
    
    % ���еĲ���
    filesname = dir(base_path);
    names = {filesname.name};
    algorithms = names(3:end);
    data = cell(1,3);
    
    % ��ȡ�� 'FUN*.tsv'Ϊ��׺���ļ�    
    for i=1:length(algorithms)
        name=char(fullfile(base_path,algorithms(i),'FUN0.tsv'));
        data(i) = {csvread(name)};
    end
    
    %% ����Cָ��
    for i=1:length(algorithms)
        for j=1:length(algorithms)
            if(i ~= j)
                fprintf('C(%s,%s)=%f\n',char(algorithms(i)),char(algorithms(j)),C(cell2mat(data(i)),cell2mat(data(j))));
            end
        end
    end
end