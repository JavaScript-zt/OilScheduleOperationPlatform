path = 'times.csv';
fid = fopen(path);
% ��ȡ����
title = textscan(fid, '%s%s%s%s%s%s',1,'delimiter', ',');
c1 = cell2mat(title{1});
c2 = cell2mat(title{2});
c3 = cell2mat(title{3});
c4 = cell2mat(title{4});
c5 = cell2mat(title{5});
c6 = cell2mat(title{6});
% ��ȡ����
data = textscan(fid, '%d%d%d%d%d%d','delimiter', ',');
data = cell2mat(data);

boxplot(data);

xlim([0, 7]);%ֻ�趨x��Ļ��Ʒ�Χ
 
set(gca,'XTick',[0:1:7]) %�ı�x����������ʾ ������Ϊ2
set(gca,'FontSize',16); %��������������ִ�С������legend���ִ�С
set(gca,'xticklabel',{'',c1,c2,c3,c4,c5,c6,''});%����X��Ŀ̶ȱ�ǩ